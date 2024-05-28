package com.edgarmontero.proyectoDam.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentCrearConsultaBinding;
import com.edgarmontero.proyectoDam.utils.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class CrearConsultaFragment extends Fragment {

    private FragmentCrearConsultaBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCrearConsultaBinding.inflate(inflater, container, false);

        setupViewBindings();

        return binding.getRoot();
    }

    private void setupViewBindings() {
        final EditText etpacienteId = binding.etPacienteId;
        final EditText ettipoConsulta = binding.etTipoConsulta;
        final EditText etdescripcion = binding.etDescripcionConsulta;
        final EditText etfecha = binding.etFechaConsulta;
        final Spinner spinnerEstadoConsulta = binding.spinnerEstadoConsulta;

        binding.btnCrearConsulta.setOnClickListener(v -> {
            String estadoConsulta = spinnerEstadoConsulta.getSelectedItem().toString();
            crearConsulta(etpacienteId.getText().toString(), ettipoConsulta.getText().toString(), etdescripcion.getText().toString(), etfecha.getText().toString(), estadoConsulta);
        });

        etfecha.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);
            int hour = calendario.get(Calendar.HOUR_OF_DAY);
            int minute = calendario.get(Calendar.MINUTE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                (timePicker, hourOfDay, minuteOfHour) -> {
                                    String fechaSeleccionada = String.format("%04d-%02d-%02d %02d:%02d:%02d", year1, monthOfYear + 1, dayOfMonth, hourOfDay, minuteOfHour, 0);
                                    etfecha.setText(fechaSeleccionada);
                                }, hour, minute, true);
                        timePickerDialog.show();
                    }, year, month, day);

            datePickerDialog.show();
        });
    }

    private void crearConsulta(String pacienteId, String tipoConsulta, String descripcion, String fecha, String estadoConsulta) {
        if (areFieldsValid(pacienteId, tipoConsulta, descripcion, fecha)) {
            crearConsultaData(pacienteId, tipoConsulta, descripcion, fecha, estadoConsulta);
        }
    }

    private boolean areFieldsValid(String pacienteId, String tipoConsulta, String descripcion, String fecha) {
        Context context = getContext();
        if (pacienteId.isEmpty() || tipoConsulta.isEmpty() || descripcion.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Validator.validarDNI(pacienteId, context)) {
            return false;
        }

        if (!Validator.isDateValid(fecha, context)) {
            return false;
        }

        return true;
    }

    private void crearConsultaData(String pacienteId, String tipoConsulta, String descripcion, String fecha, String estadoConsulta) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String dniMedico = sharedPreferences.getString("dni_medico", "");

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "guardarConsulta.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("dni_medico", "UTF-8") + "=" + URLEncoder.encode(dniMedico, "UTF-8");
                data += "&" + URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(pacienteId, "UTF-8");
                data += "&" + URLEncoder.encode("tipo_consulta", "UTF-8") + "=" + URLEncoder.encode(tipoConsulta, "UTF-8");
                data += "&" + URLEncoder.encode("descripcion", "UTF-8") + "=" + URLEncoder.encode(descripcion, "UTF-8");
                data += "&" + URLEncoder.encode("fecha", "UTF-8") + "=" + URLEncoder.encode(fecha, "UTF-8");
                data += "&" + URLEncoder.encode("estado_consulta", "UTF-8") + "=" + URLEncoder.encode(estadoConsulta, "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                processServerResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
        thread.start();
    }

    private void processServerResponse(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();
        in.close();
        conn.disconnect();

        if (result.toString().contains("success")) {
            String finalMessage = "Consulta creada con éxito";
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), finalMessage, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(CrearConsultaFragment.this)
                        .navigate(R.id.action_nav_crear_consulta_to_nav_home);

            });
        } else {
            String finalMessage2 = "Error al crear consulta";
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), finalMessage2, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
