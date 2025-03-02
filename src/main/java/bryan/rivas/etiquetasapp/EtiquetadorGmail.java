package bryan.rivas.etiquetasapp;

import javax.mail.*;
import java.util.*;
import java.util.stream.Collectors;

public class EtiquetadorGmail {
    private static final String HOST = "imap.gmail.com";
    private static final String USUARIO = "pspPracticaBryan@gmail.com";
    private static final String CONTRASENA = "bqpt ztce qmso wpor";
    private static final int NUM_MAX_CORREOS = 7;
    private static final String ETIQUETA_HECHO = "Done";
    private static final String ETIQUETA_EN_PROGRESO = "Work in Progress";
    private static final String ETIQUETA_PENDIENTE = "To be Done";

    public void etiquetarCorreos() {
        try (Store tienda = obtenerTienda()) {
            Folder bandejaEntrada = tienda.getDefaultFolder().getFolder("INBOX");
            bandejaEntrada.open(Folder.READ_WRITE);

            Message[] correos = bandejaEntrada.getMessages();
            int totalCorreos = Math.min(correos.length, NUM_MAX_CORREOS);

            for (int i = 0; i < totalCorreos; i++) {
                String etiqueta = obtenerEtiquetaPorIndice(i);
                copiarCorreoAEtiqueta(tienda, bandejaEntrada, correos[i], etiqueta);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Store obtenerTienda() throws MessagingException {
        Properties propiedades = new Properties();
        propiedades.put("mail.store.protocol", "imaps");
        Session sesion = Session.getInstance(propiedades);
        Store tienda = sesion.getStore();
        tienda.connect(HOST, USUARIO, CONTRASENA);
        return tienda;
    }

    private String obtenerEtiquetaPorIndice(int indice) {
        if (indice < 3) return ETIQUETA_HECHO;
        if (indice == 3) return ETIQUETA_EN_PROGRESO;
        return ETIQUETA_PENDIENTE;
    }

    private void copiarCorreoAEtiqueta(Store tienda, Folder bandejaEntrada, Message correo, String etiqueta) throws MessagingException {
        Folder carpetaEtiqueta = tienda.getFolder(etiqueta);
        if (!carpetaEtiqueta.exists()) {
            carpetaEtiqueta.create(Folder.HOLDS_MESSAGES);
        }
        bandejaEntrada.copyMessages(new Message[]{correo}, carpetaEtiqueta);
    }

    public Map<String, List<String>> obtenerCorreosEtiquetados() {
        Map<String, List<String>> correosEtiquetados = new HashMap<>();
        try (Store tienda = obtenerTienda()) {
            for (String etiqueta : Arrays.asList(ETIQUETA_HECHO, ETIQUETA_EN_PROGRESO, ETIQUETA_PENDIENTE)) {
                Folder carpeta = tienda.getFolder(etiqueta);
                if (carpeta.exists()) {
                    carpeta.open(Folder.READ_ONLY);
                    List<String> correosInfo = Arrays.stream(carpeta.getMessages())
                            .map(m -> obtenerInfoCorreo(m))
                            .collect(Collectors.toList());
                    correosEtiquetados.put(etiqueta, correosInfo);
                    carpeta.close(false);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return correosEtiquetados;
    }

    private String obtenerInfoCorreo(Message correo) {
        try {
            String asunto = correo.getSubject() != null ? correo.getSubject() : "(Sin Asunto)";
            String remitente = correo.getFrom() != null ? correo.getFrom()[0].toString() : "(Sin Remitente)";
            return "Asunto: " + asunto + " | Remitente: " + remitente;
        } catch (MessagingException e) {
            return "(Error al obtener datos del correo)";
        }
    }
}
