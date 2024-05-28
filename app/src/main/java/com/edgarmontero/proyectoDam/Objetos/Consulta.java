package com.edgarmontero.proyectoDam.Objetos;

import java.util.Date;

public class Consulta {
    private Long idConsulta;
    private String idMedico;
    private String idPaciente;
    private String tipoConsulta;
    private String descripcionConsulta;
    private String fechaConsulta;
    public Consulta(String tipoConsulta, String descripcionConsulta, String fechaConsulta, Long idConsulta, String idMedico, String idPaciente) {
        this.tipoConsulta = tipoConsulta;
        this.descripcionConsulta = descripcionConsulta;
        this.fechaConsulta = fechaConsulta;
        this.idConsulta = idConsulta;
        this.idMedico = idMedico;
        this.idPaciente = idPaciente;
    }


    public Long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(String idMedico) {
        this.idMedico = idMedico;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(String idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getTipoConsulta() {
        return tipoConsulta;
    }

    public void setTipoConsulta(String tipoConsulta) {
        this.tipoConsulta = tipoConsulta;
    }

    public String getDescripcionConsulta() {
        return descripcionConsulta;
    }

    public void setDescripcionConsulta(String descripcionConsulta) {
        this.descripcionConsulta = descripcionConsulta;
    }

    public String getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(String fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }
}
