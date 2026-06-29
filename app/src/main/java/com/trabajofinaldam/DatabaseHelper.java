package com.trabajofinaldam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper — Capa de acceso a datos (SQLite).
 *
 * Patrón: Singleton + SQLiteOpenHelper
 *
 * En MVVM esta clase es parte del REPOSITORY (o actúa como tal
 * mientras no tengas Room). El ViewModel la llama para leer/escribir;
 * nunca la llama la Activity directamente.
 *
 * Tabla: tareas
 * ┌────┬─────────────┬──────────────┬──────────┬────────────┬─────────────────┬──────┐
 * │ id │ descripcion │ fecha_limite │ prioridad│ completada │ auto_programada │ hora │
 * └────┴─────────────┴──────────────┴──────────┴────────────┴─────────────────┴──────┘
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ===================================================================
    // CONSTANTES DE LA BASE DE DATOS
    // ===================================================================
    private static final String DB_NAME    = "ecotask.db";
    private static final int    DB_VERSION = 1;

    // --- Tabla y columnas ---
    public static final String TABLE_TAREAS        = "tareas";
    public static final String COL_ID              = "id";
    public static final String COL_DESCRIPCION     = "descripcion";
    public static final String COL_FECHA_LIMITE    = "fecha_limite";
    public static final String COL_PRIORIDAD       = "prioridad";
    public static final String COL_COMPLETADA      = "completada";      // 0 o 1
    public static final String COL_AUTO_PROGRAMADA = "auto_programada"; // 0 o 1
    public static final String COL_HORA            = "hora";

    // ===================================================================
    // SINGLETON — un solo Helper por proceso de la app
    // ===================================================================
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Constructor privado para el Singleton
    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ===================================================================
    // onCreate — Se ejecuta UNA SOLA VEZ al crear la BD por primera vez
    // ===================================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * CREATE TABLE tareas (
         *   id              INTEGER PRIMARY KEY AUTOINCREMENT,
         *   descripcion     TEXT    NOT NULL,
         *   fecha_limite    TEXT    NOT NULL,
         *   prioridad       TEXT    NOT NULL DEFAULT 'MEDIA',
         *   completada      INTEGER NOT NULL DEFAULT 0,
         *   auto_programada INTEGER NOT NULL DEFAULT 0,
         *   hora            TEXT    NOT NULL DEFAULT ''
         * );
         */
        String createTable =
                "CREATE TABLE " + TABLE_TAREAS + " (" +
                        COL_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_DESCRIPCION     + " TEXT    NOT NULL, "                  +
                        COL_FECHA_LIMITE    + " TEXT    NOT NULL, "                  +
                        COL_PRIORIDAD       + " TEXT    NOT NULL DEFAULT 'MEDIA', "  +
                        COL_COMPLETADA      + " INTEGER NOT NULL DEFAULT 0, "        +
                        COL_AUTO_PROGRAMADA + " INTEGER NOT NULL DEFAULT 0, "        +
                        COL_HORA            + " TEXT    NOT NULL DEFAULT ''"         +
                        ");";

        db.execSQL(createTable);
    }

    // ===================================================================
    // onUpgrade — Se ejecuta cuando subes DB_VERSION (migraciones)
    // ===================================================================
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Estrategia simple: borrar y recrear.
        // En producción aplica ALTER TABLE para no perder datos.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAREAS);
        onCreate(db);
    }

    // ===================================================================
    // CREATE — Insertar una nueva tarea
    // Retorna el id generado, o -1 si hubo error.
    // ===================================================================
    public long insertarTarea(TaskModel tarea) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DESCRIPCION,     tarea.getDescripcion());
        values.put(COL_FECHA_LIMITE,    tarea.getFechaLimite());
        values.put(COL_PRIORIDAD,       tarea.getPrioridad());
        values.put(COL_COMPLETADA,      tarea.isCompletada()     ? 1 : 0);
        values.put(COL_AUTO_PROGRAMADA, tarea.isAutoProgramada() ? 1 : 0);
        values.put(COL_HORA,            tarea.getHora());

        long nuevoId = db.insert(TABLE_TAREAS, null, values);
        db.close();
        return nuevoId;
    }

    // ===================================================================
    // READ — Leer todas las tareas PENDIENTES (completada = 0)
    // Esta es la lista que muestra el "Calendario Inteligente".
    // ===================================================================
    public List<TaskModel> obtenerTareasPendientes() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SELECT * FROM tareas WHERE completada = 0 ORDER BY hora ASC
        Cursor cursor = db.query(
                TABLE_TAREAS,               // tabla
                null,                       // columnas (null = todas)
                COL_COMPLETADA + " = ?",    // WHERE
                new String[]{"0"},          // args del WHERE
                null,                       // GROUP BY
                null,                       // HAVING
                COL_HORA + " ASC"           // ORDER BY hora
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TaskModel tarea = cursorToTask(cursor);
                lista.add(tarea);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return lista;
    }

    // ===================================================================
    // READ — Leer TODAS las tareas (para estadísticas de progreso)
    // El ViewModel calcula el porcentaje: completadas / total * 100
    // ===================================================================
    public List<TaskModel> obtenerTodasLasTareas() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TAREAS, null, null, null, null, null,
                COL_HORA + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                lista.add(cursorToTask(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return lista;
    }

    // ===================================================================
    // UPDATE — Marcar una tarea como completada
    // Llamado cuando el usuario tilda un ítem en el Modo Enfoque.
    // ===================================================================
    public int marcarCompletada(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_COMPLETADA, 1);

        int filasAfectadas = db.update(
                TABLE_TAREAS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(tareaId)}
        );

        db.close();
        return filasAfectadas;
    }

    // ===================================================================
    // HELPER PRIVADO — Convierte una fila del Cursor en un TaskModel
    // Centraliza la lectura de columnas para no repetir código.
    // ===================================================================
    private TaskModel cursorToTask(Cursor cursor) {
        int     id              = cursor.getInt   (cursor.getColumnIndexOrThrow(COL_ID));
        String  descripcion     = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
        String  fechaLimite     = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_LIMITE));
        String  prioridad       = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIORIDAD));
        boolean completada      = cursor.getInt   (cursor.getColumnIndexOrThrow(COL_COMPLETADA))      == 1;
        boolean autoProgramada  = cursor.getInt   (cursor.getColumnIndexOrThrow(COL_AUTO_PROGRAMADA)) == 1;
        String  hora            = cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA));

        return new TaskModel(id, descripcion, fechaLimite,
                prioridad, completada, autoProgramada, hora);
    }
}