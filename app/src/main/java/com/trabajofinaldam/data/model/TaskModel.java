package com.trabajofinaldam.data.model;

public class TaskModel {
    public static final String PRIORIDAD_BAJA  = "BAJA";
    public static final String PRIORIDAD_MEDIA = "MEDIA";
    public static final String PRIORIDAD_ALTA  = "ALTA";

    private int     id;
    private String  titulo;
    private String  descripcion;
    private String  fechaLimite;
    private String  prioridad;
    private boolean completada;
    private boolean autoProgramada;
    private String  hora;
    private boolean iniciada;
    private int     totalSubtareas;
    private int     subtareasCompletadas;
    private String  fechaCompletada;

    public TaskModel(int id, String titulo, String descripcion, String fechaLimite,
                     String prioridad, boolean completada,
                     boolean autoProgramada, String hora, boolean iniciada,
                     int totalSubtareas, int subtareasCompletadas) {
        this(id, titulo, descripcion, fechaLimite, prioridad, completada, autoProgramada, hora, iniciada, totalSubtareas, subtareasCompletadas, null);
    }

    public TaskModel(int id, String titulo, String descripcion, String fechaLimite,
                     String prioridad, boolean completada,
                     boolean autoProgramada, String hora, boolean iniciada,
                     int totalSubtareas, int subtareasCompletadas, String fechaCompletada) {
        this.id             = id;
        this.titulo         = titulo;
        this.descripcion    = descripcion;
        this.fechaLimite    = fechaLimite;
        this.prioridad      = prioridad;
        this.completada     = completada;
        this.autoProgramada = autoProgramada;
        this.hora           = hora;
        this.iniciada       = iniciada;
        this.totalSubtareas = totalSubtareas;
        this.subtareasCompletadas = subtareasCompletadas;
        this.fechaCompletada = fechaCompletada;
    }

    public TaskModel(int id, String titulo, String descripcion, String fechaLimite,
                     String prioridad, boolean completada,
                     boolean autoProgramada, String hora, boolean iniciada) {
        this(id, titulo, descripcion, fechaLimite, prioridad, completada, autoProgramada, hora, iniciada, 0, 0, null);
    }

    public TaskModel(String titulo, String descripcion, String fechaLimite,
                     String prioridad, boolean completada,
                     boolean autoProgramada, String hora) {
        this(-1, titulo, descripcion, fechaLimite, prioridad, completada, autoProgramada, hora, false, 0, 0, null);
    }

    public int     getId()             { return id; }
    public String  getTitulo()         { return titulo; }
    public String  getDescripcion()    { return descripcion; }
    public String  getFechaLimite()    { return fechaLimite; }
    public String  getPrioridad()      { return prioridad; }
    public boolean isCompletada()      { return completada; }
    public boolean isAutoProgramada()  { return autoProgramada; }
    public String  getHora()           { return hora; }
    public boolean isIniciada()        { return iniciada; }
    public int     getTotalSubtareas() { return totalSubtareas; }
    public int     getSubtareasCompletadas() { return subtareasCompletadas; }
    public String  getFechaCompletada() { return fechaCompletada; }

    public void setId(int id)                        { this.id = id; }
    public void setTitulo(String titulo)             { this.titulo = titulo; }
    public void setDescripcion(String descripcion)   { this.descripcion = descripcion; }
    public void setFechaLimite(String fechaLimite)   { this.fechaLimite = fechaLimite; }
    public void setPrioridad(String prioridad)       { this.prioridad = prioridad; }
    public void setCompletada(boolean completada)    { this.completada = completada; }
    public void setAutoProgramada(boolean v)         { this.autoProgramada = v; }
    public void setHora(String hora)                 { this.hora = hora; }
    public void setIniciada(boolean iniciada)        { this.iniciada = iniciada; }
    public void setTotalSubtareas(int n)             { this.totalSubtareas = n; }
    public void setSubtareasCompletadas(int n)       { this.subtareasCompletadas = n; }
    public void setFechaCompletada(String f)         { this.fechaCompletada = f; }

    public boolean isVencida() {
        if (completada || fechaLimite == null || fechaLimite.isEmpty()) return false;
        try {
            String formatStr = "dd/MM/yyyy";
            String dateTimeStr = fechaLimite;

            if (hora != null && !hora.isEmpty()) {
                formatStr += " HH:mm";
                dateTimeStr += " " + hora;
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(formatStr, java.util.Locale.getDefault());
            java.util.Date limite = sdf.parse(dateTimeStr);
            java.util.Date ahora = new java.util.Date();

            return limite != null && ahora.after(limite);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "TaskModel{id=" + id +
                ", titulo='" + titulo + '\'' +
                ", iniciada=" + iniciada +
                ", completada=" + completada + '}';
    }
}
