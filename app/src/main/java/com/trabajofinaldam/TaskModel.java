package com.trabajofinaldam;


/**
 * TaskModel — Modelo de datos (capa Model en MVVM).
 *
 * Representa una fila de la tabla 'tareas' en SQLite.
 * Cada campo mapea directamente a una columna de la BD.
 *
 * Tabla: tareas
 * ┌────┬─────────────┬──────────────┬──────────┬────────────┬──────────────────┐
 * │ id │ descripcion │ fecha_limite │ prioridad│ completada │ auto_programada  │
 * └────┴─────────────┴──────────────┴──────────┴────────────┴──────────────────┘
 */
public class TaskModel {

    // ---------------------------------------------------------------
    // Constantes de prioridad (evitan magic strings en el código)
    // ---------------------------------------------------------------
    public static final String PRIORIDAD_BAJA  = "BAJA";
    public static final String PRIORIDAD_MEDIA = "MEDIA";
    public static final String PRIORIDAD_ALTA  = "ALTA";

    // ---------------------------------------------------------------
    // Campos — mapean 1:1 con columnas de SQLite
    // ---------------------------------------------------------------
    private int     id;              // PK autoincrement
    private String  descripcion;     // Texto de la tarea
    private String  fechaLimite;     // Formato "dd/MM/yyyy"
    private String  prioridad;       // "BAJA" | "MEDIA" | "ALTA"
    private boolean completada;      // 0 = pendiente, 1 = completada
    private boolean autoProgramada;  // true = badge "Auto-programada" en UI
    private String  hora;            // Hora programada "HH:mm" para calendario

    // ---------------------------------------------------------------
    // Constructor completo (usado al leer de SQLite)
    // ---------------------------------------------------------------
    public TaskModel(int id, String descripcion, String fechaLimite,
                     String prioridad, boolean completada,
                     boolean autoProgramada, String hora) {
        this.id             = id;
        this.descripcion    = descripcion;
        this.fechaLimite    = fechaLimite;
        this.prioridad      = prioridad;
        this.completada     = completada;
        this.autoProgramada = autoProgramada;
        this.hora           = hora;
    }

    // Constructor sin id (usado al insertar — SQLite genera el id)
    public TaskModel(String descripcion, String fechaLimite,
                     String prioridad, boolean completada,
                     boolean autoProgramada, String hora) {
        this(-1, descripcion, fechaLimite, prioridad,
                completada, autoProgramada, hora);
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public int     getId()             { return id; }
    public String  getDescripcion()    { return descripcion; }
    public String  getFechaLimite()    { return fechaLimite; }
    public String  getPrioridad()      { return prioridad; }
    public boolean isCompletada()      { return completada; }
    public boolean isAutoProgramada()  { return autoProgramada; }
    public String  getHora()           { return hora; }

    public void setId(int id)                        { this.id = id; }
    public void setDescripcion(String descripcion)   { this.descripcion = descripcion; }
    public void setFechaLimite(String fechaLimite)   { this.fechaLimite = fechaLimite; }
    public void setPrioridad(String prioridad)       { this.prioridad = prioridad; }
    public void setCompletada(boolean completada)    { this.completada = completada; }
    public void setAutoProgramada(boolean v)         { this.autoProgramada = v; }
    public void setHora(String hora)                 { this.hora = hora; }

    // ---------------------------------------------------------------
    // Útil para logs y debug
    // ---------------------------------------------------------------
    @Override
    public String toString() {
        return "TaskModel{id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", fechaLimite='" + fechaLimite + '\'' +
                ", prioridad='" + prioridad + '\'' +
                ", completada=" + completada +
                ", autoProgramada=" + autoProgramada +
                ", hora='" + hora + '\'' + '}';
    }
}
