package com.edgarmontero.proyectoDam.utils;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Validator {

    private static final char[] LETRAS_DNI = {
        'T', 'R', 'W', 'A', 'G', 'M', 'Y', 'F', 'P', 'D', 'X', 'B', 'N', 'J', 'Z', 'S', 'Q', 'V', 'H', 'L', 'C', 'K', 'E'
    };

    public static boolean validarDNI(String dni, Context context) {
        if (dni == null || dni.length() != 9 || !Character.isLetter(dni.charAt(8))) {
            Toast.makeText(context, "DNI no válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        String parteNumerica = dni.substring(0, 8);
        char letra = Character.toUpperCase(dni.charAt(8));

        try {
            int numerosDNI = Integer.parseInt(parteNumerica);
            int indice = numerosDNI % 23;
            char letraCalculada = LETRAS_DNI[indice];
            return letra == letraCalculada;
        } catch (NumberFormatException e) {
            Toast.makeText(context, "DNI no válido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean isDateValid(String fecha, Context context) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNac = sdf.parse(fecha);
            Date fechaActual = new Date();
            if (!fechaNac.before(fechaActual)) {
                Toast.makeText(context, "La fecha debe ser anterior al día de hoy", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(context, "Formato de fecha no válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isFechaNacimintoValida(String fecha, Context context) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNac = sdf.parse(fecha);
            Date fechaActual = new Date();
            if (!fechaNac.before(fechaActual)) { // Cambiar "before" a "after"
                Toast.makeText(context, "La fecha debe ser anterior al día de hoy", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(context, "Formato de fecha no válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isPhoneValid(String telefono, Context context) {
        if (!telefono.matches("\\d{9}")) {
            Toast.makeText(context, "El teléfono debe tener 9 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
