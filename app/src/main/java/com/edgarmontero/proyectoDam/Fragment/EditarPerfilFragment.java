package com.edgarmontero.proyectoDam.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentEditarPerfilBinding;
import com.edgarmontero.proyectoDam.utils.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class EditarPerfilFragment extends Fragment {

    private FragmentEditarPerfilBinding binding;
    private EditText etNombre, etDni, etEspecialidad;
    private Button btnSave;
    private String dniMedico;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPerfilBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        dniMedico = sharedPreferences.getString("dni_medico", "");

        setupViewBindings();
        buscarUsuario(dniMedico);

        return binding.getRoot();
    }

    private void setupViewBindings() {
        etNombre = binding.etNombreMedico;
        etDni = binding.etDniMedico;
        etEspecialidad = binding.etEspecialidad;
        btnSave = binding.btnGuardarPaciente;

        btnSave.setOnClickListener(v -> {
            String dniMedico = etDni.getText().toString();
            String nombre = etNombre.getText().toString();
            String especialidad = etEspecialidad.getText().toString();
            saveMedico(dniMedico, nombre, especialidad);
        });
    }

    private void buscarUsuario(String dniMedico) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "buscarMedico.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("dni_medico", "UTF-8") + "=" + URLEncoder.encode(dniMedico, "UTF-8");

                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    JSONObject jsonObject = new JSONObject(response.toString());

                    if(jsonObject.has("error")) {
                        String errorMsg = jsonObject.getString("error");
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show());
                    } else {
                        updateEditTexts(jsonObject);
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en la conexión: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error técnico al procesar la búsqueda", Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
    }

    private void updateEditTexts(JSONObject jsonObject) throws JSONException {
        getActivity().runOnUiThread(() -> {
            try {
                etNombre.setText(jsonObject.getString("nombre"));
                etDni.setText(jsonObject.getString("dni_medico"));
                etEspecialidad.setText(jsonObject.getString("especialidad"));
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error en el formato de los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMedico(String dni, String nombre, String especialidad) {
        if (areFieldsValid(dni, nombre, especialidad)) {
            saveMedicoData(dni, nombre, especialidad);
        }
    }

    private boolean areFieldsValid(String dni, String nombre, String especialidad) {
        Context context = getContext();
        if (dni.isEmpty() || nombre.isEmpty() || especialidad.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Validator.validarDNI(dni, context)) {
            return false;
        }

        return true;
    }

    private void saveMedicoData(String dni, String nombre, String especialidad) {

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "actualizarMedico.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("dni_medico", "UTF-8") + "=" + URLEncoder.encode(dniMedico, "UTF-8");
                data += "&" + URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombre, "UTF-8");
                data += "&" + URLEncoder.encode("especialidad", "UTF-8") + "=" + URLEncoder.encode(especialidad, "UTF-8");

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

    private void processServerResponse(HttpURLConnection conn) {
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = reader.readLine();
                JSONObject jsonObject = new JSONObject(response);
                getActivity().runOnUiThread(() -> {
                    try {
                        if (jsonObject.has("error")) {
                            Toast.makeText(getContext(), jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_nav_editar_perfil_to_nav_home);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                });
                reader.close();
            } else {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en la conexión: " + responseCode, Toast.LENGTH_SHORT).show());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error técnico al actualizar los datos", Toast.LENGTH_SHORT).show());
        }
    }

}