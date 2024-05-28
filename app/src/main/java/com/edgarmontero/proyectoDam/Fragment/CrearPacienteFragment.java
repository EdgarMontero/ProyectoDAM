package com.edgarmontero.proyectoDam.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.edgarmontero.proyectoDam.Objetos.User;
import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentCrearPacienteBinding;
import com.edgarmontero.proyectoDam.utils.Validator;

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
import java.util.List;
import java.util.Map;

public class CrearPacienteFragment extends Fragment {

    private FragmentCrearPacienteBinding binding;
    List<User> usersList = new ArrayList<>();

    private EditText etDni, etNombre, etFechaNacimiento, etDireccion, etTelefono;
    AutoCompleteTextView autoCompleteTextViewUser;
    Map<String, Integer> userNameToIdMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearPacienteBinding.inflate(inflater, container, false);

        autoCompleteTextViewUser = binding.autoCompleteTextViewUser;
        fetchUsers();
        setupViewBindings();

        return binding.getRoot();
    }

    private void setupViewBindings() {
        etDni = binding.etDniPaciente;
        etNombre = binding.etNombrePaciente;
        etFechaNacimiento = binding.etFechaNacimiento;
        etDireccion = binding.etDireccion;
        etTelefono = binding.etTelefono;
        Button btnSave = binding.btnGuardarPaciente;

        btnSave.setOnClickListener(v -> savePatient(etDni.getText().toString(), etNombre.getText().toString(),
                etFechaNacimiento.getText().toString(), etDireccion.getText().toString(),
                etTelefono.getText().toString()));

        setupDatePicker(etFechaNacimiento);
    }

    private void setupDatePicker(EditText etFechaNacimiento) {
        etFechaNacimiento.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        etFechaNacimiento.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void fetchUsers() {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip)+"fetchUsers.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                in.close();

                parseJson(result.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void parseJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("id_user");
                String name = obj.getString("name");
                usersList.add(new User(id, name));
            }

            getActivity().runOnUiThread(() -> updateUI());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        for (User user : usersList) {
            userNameToIdMap.put(user.getName(), user.getIdUser());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, extractUserNames(usersList));
        autoCompleteTextViewUser.setAdapter(adapter);
    }

    private List<String> extractUserNames(List<User> usersList) {
        List<String> names = new ArrayList<>();
        for (User user : usersList) {
            names.add(user.getName());
        }
        return names;
    }

    private void savePatient(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        if (areFieldsValid(dni, nombre, fechaNacimiento, direccion, telefono)) {
            savePatientData(dni, nombre, fechaNacimiento, direccion, telefono);
        }
    }

    private boolean areFieldsValid(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        Context context = getContext();
        if (dni.isEmpty() || nombre.isEmpty() || fechaNacimiento.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Validator.validarDNI(dni, context)) {
            return false;
        }

        if (!Validator.isDateValid(fechaNacimiento, context)) {
            return false;
        }

        if (!Validator.isPhoneValid(telefono, context)) {
            return false;
        }

        return true;
    }

    private void savePatientData(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        final String finalDni = dni.toUpperCase();

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "guardarPaciente.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(finalDni, "UTF-8");
                data += "&" + URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombre, "UTF-8");
                data += "&" + URLEncoder.encode("fecha_nacimiento", "UTF-8") + "=" + URLEncoder.encode(fechaNacimiento, "UTF-8");
                data += "&" + URLEncoder.encode("direccion", "UTF-8") + "=" + URLEncoder.encode(direccion, "UTF-8");
                data += "&" + URLEncoder.encode("telefono", "UTF-8") + "=" + URLEncoder.encode(telefono, "UTF-8");

                String selectedUserName = autoCompleteTextViewUser.getText().toString();
                Integer userId = userNameToIdMap.get(selectedUserName);
                if (userId == null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                data += "&" + URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                processServerResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT). show();
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
            String finalMessage = "Paciente guardado con éxito";
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), finalMessage, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(CrearPacienteFragment.this)
                        .navigate(R.id.action_nav_crear_paciente_to_nav_home);

            });
        } else {
            String finalMessage2 = "Error al guardar el paciente";
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
