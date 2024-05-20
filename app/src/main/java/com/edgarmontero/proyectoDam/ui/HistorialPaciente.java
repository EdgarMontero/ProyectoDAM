package com.edgarmontero.proyectoDam.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentHistorialPacienteBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class HistorialPaciente extends Fragment {

    private FragmentHistorialPacienteBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistorialPacienteBinding.inflate(inflater, container, false);

        setupListView();
        setupSearch();
        setupDatePicker();

        return binding.getRoot();
    }

    private void setupDatePicker() {
        binding.editTextFechaFin.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        binding.editTextFechaFin.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });

        binding.editTextFechaInicio.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        binding.editTextFechaInicio.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.listViewConsultas.setAdapter(adapter);

    }

    private void showEditDialog(String item, String consultaId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_editar_consulta, null);
        builder.setView(dialogView);

        EditText editTextTipoConsulta = dialogView.findViewById(R.id.editTextTipoConsulta);
        EditText editTextDescripcionConsulta = dialogView.findViewById(R.id.editTextDescripcionConsulta);
        EditText editTextFechaConsulta = dialogView.findViewById(R.id.editTextFechaConsulta);

        String[] parts = item.split("\\n");
        editTextTipoConsulta.setText(parts[0].substring(parts[0].indexOf(':') + 1).trim());
        editTextDescripcionConsulta.setText(parts[1].substring(parts[1].indexOf(':') + 1).trim());
        editTextFechaConsulta.setText(parts[2].substring(parts[2].indexOf(':') + 1).trim());

        builder.setPositiveButton("Guardar Cambios", (dialog, which) -> {
            guardarCambios(consultaId, editTextTipoConsulta.getText().toString(),
                    editTextDescripcionConsulta.getText().toString(),
                    editTextFechaConsulta.getText().toString());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Agregar botón para borrar la consulta
        builder.setNeutralButton("Borrar", (dialog, which) -> {
            borrarConsulta(consultaId); // Método para borrar la consulta
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void guardarCambios(String consultaId, String tipo, String descripcion, String fecha) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "actualizarConsulta.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("id_consulta", "UTF-8") + "=" + URLEncoder.encode(consultaId, "UTF-8") +
                        "&" + URLEncoder.encode("tipo_consulta", "UTF-8") + "=" + URLEncoder.encode(tipo, "UTF-8") +
                        "&" + URLEncoder.encode("descripcion", "UTF-8") + "=" + URLEncoder.encode(descripcion, "UTF-8") +
                        "&" + URLEncoder.encode("fecha", "UTF-8") + "=" + URLEncoder.encode(fecha, "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject response = new JSONObject(result.toString());

                getActivity().runOnUiThread(() -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(getContext(), "Cambios guardados con éxito", Toast.LENGTH_SHORT).show();
                            performSearch(binding.editTextDniPaciente.getText().toString()); // Actualizar lista
                        } else {
                            Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });

                reader.close();
                in.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
        thread.start();
    }

    private void setupSearch() {
        binding.buttonBuscarPaciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch(binding.editTextDniPaciente.getText().toString());
            }
        });

        binding.editTextDniPaciente.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.editTextDniPaciente.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String dni) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "buscarConsultasPaciente.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String fechaInicio = binding.editTextFechaInicio.getText().toString();
                String fechaFin = binding.editTextFechaFin.getText().toString();

                StringBuilder data = new StringBuilder();
                data.append(URLEncoder.encode("dni_paciente", "UTF-8")).append("=").append(URLEncoder.encode(dni.toUpperCase(), "UTF-8"));

                if (!fechaInicio.isEmpty()) {
                    data.append("&").append(URLEncoder.encode("fecha_inicio", "UTF-8")).append("=").append(URLEncoder.encode(fechaInicio, "UTF-8"));
                }
                if (!fechaFin.isEmpty()) {
                    data.append("&").append(URLEncoder.encode("fecha_fin", "UTF-8")).append("=").append(URLEncoder.encode(fechaFin, "UTF-8"));
                }

                writer.write(data.toString());
                writer.flush();
                writer.close();
                os.close();

                processSearchResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
        thread.start();
    }

    private void processSearchResponse(HttpURLConnection conn) throws IOException {
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

        final String response = result.toString();
        getActivity().runOnUiThread(() -> {
            updateListView(response);
        });
    }

    private void updateListView(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            ArrayList<String> consultasList = new ArrayList<>();
            final HashMap<Integer, String> consultaIds = new HashMap<>(); // Para almacenar los IDs

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int idConsulta = obj.getInt("id_consulta");
                String consulta = "Consulta: " + obj.getString("tipo_consulta") + "\nDescripción: " + obj.getString("descripcion_consulta") + "\nFecha: " + obj.getString("fecha_consulta");
                consultasList.add(consulta);
                consultaIds.put(i, String.valueOf(idConsulta)); // Guarda el ID con el índice de la lista
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, consultasList);
            binding.listViewConsultas.setAdapter(adapter);
            binding.listViewConsultas.setOnItemClickListener((parent, view, position, id) -> {
                String item = adapter.getItem(position);
                String consultaId = consultaIds.get(position); // Obtener el ID de la consulta
                showEditDialog(item, consultaId); // Pasar el ID al diálogo de edición
            });

        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void borrarConsulta(String consultaId) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "eliminarConsulta.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("id_consulta", "UTF-8") + "=" + URLEncoder.encode(consultaId, "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject response = new JSONObject(result.toString());

                getActivity().runOnUiThread(() -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(getContext(), "Consulta eliminada", Toast.LENGTH_SHORT).show();
                            performSearch(binding.editTextDniPaciente.getText().toString()); // Actualizar lista
                        } else {
                            Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });

                reader.close();
                in.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
        thread.start();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
