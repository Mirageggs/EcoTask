package com.trabajofinaldam.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.model.TaskModel;
import com.trabajofinaldam.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "ecotask.db";
    private static final int    DB_VERSION = 3;

    // --- Tabla tareas ---
    public static final String TABLE_TAREAS        = "tareas";
    public static final String COL_ID              = "id";
    public static final String COL_DESCRIPCION     = "descripcion";
    public static final String COL_FECHA_LIMITE    = "fecha_limite";
    public static final String COL_PRIORIDAD       = "prioridad";
    public static final String COL_COMPLETADA      = "completada";
    public static final String COL_AUTO_PROGRAMADA = "auto_programada";
    public static final String COL_HORA            = "hora";

    // --- Tabla usuarios ---
    public static final String TABLE_USUARIOS      = "usuarios";
    public static final String COL_USR_ID          = "id";
    public static final String COL_USR_NOMBRE      = "nombre";
    public static final String COL_USR_EMAIL       = "email";
    public static final String COL_USR_PASSWORD    = "password_hash";
    public static final String COL_USR_ROL         = "rol";
    public static final String COL_USR_FECHA       = "fecha_registro";

    // --- Tabla subtareas ---
    public static final String TABLE_SUBTAREAS     = "subtareas";
    public static final String COL_SUB_ID          = "id";
    public static final String COL_SUB_TAREA_ID    = "tarea_id";
    public static final String COL_SUB_DESCRIPCION = "descripcion";
    public static final String COL_SUB_COMPLETADA  = "completada";

    // --- Singleton ---
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE_TAREAS + " (" +
                COL_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DESCRIPCION     + " TEXT    NOT NULL, "                  +
                COL_FECHA_LIMITE    + " TEXT    NOT NULL, "                  +
                COL_PRIORIDAD       + " TEXT    NOT NULL DEFAULT 'MEDIA', "  +
                COL_COMPLETADA      + " INTEGER NOT NULL DEFAULT 0, "        +
                COL_AUTO_PROGRAMADA + " INTEGER NOT NULL DEFAULT 0, "        +
                COL_HORA            + " TEXT    NOT NULL DEFAULT ''"         +
            ");"
        );

        db.execSQL(
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                COL_USR_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USR_NOMBRE   + " TEXT    NOT NULL, "                  +
                COL_USR_EMAIL    + " TEXT    NOT NULL UNIQUE, "           +
                COL_USR_PASSWORD + " TEXT    NOT NULL, "                  +
                COL_USR_ROL      + " TEXT    NOT NULL DEFAULT 'GENERAL', " +
                COL_USR_FECHA    + " TEXT    NOT NULL DEFAULT ''"         +
            ");"
        );

        db.execSQL(
            "CREATE TABLE " + TABLE_SUBTAREAS + " (" +
                COL_SUB_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SUB_TAREA_ID    + " INTEGER NOT NULL, "                  +
                COL_SUB_DESCRIPCION + " TEXT    NOT NULL, "                  +
                COL_SUB_COMPLETADA  + " INTEGER NOT NULL DEFAULT 0"          +
            ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTAREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    // ===================================================================
    // TAREAS — CRUD
    // ===================================================================

    public long insertarTarea(TaskModel tarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_DESCRIPCION,     tarea.getDescripcion());
        v.put(COL_FECHA_LIMITE,    tarea.getFechaLimite());
        v.put(COL_PRIORIDAD,       tarea.getPrioridad());
        v.put(COL_COMPLETADA,      tarea.isCompletada()     ? 1 : 0);
        v.put(COL_AUTO_PROGRAMADA, tarea.isAutoProgramada() ? 1 : 0);
        v.put(COL_HORA,            tarea.getHora());
        long id = db.insert(TABLE_TAREAS, null, v);
        db.close();
        return id;
    }

    public List<TaskModel> obtenerTareasPendientes() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_TAREAS, null,
                COL_COMPLETADA + " = ?", new String[]{"0"},
                null, null, COL_HORA + " ASC");
        if (c != null && c.moveToFirst()) {
            do { lista.add(cursorToTask(c)); } while (c.moveToNext());
            c.close();
        }
        db.close();
        return lista;
    }

    public List<TaskModel> obtenerTodasLasTareas() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_TAREAS, null, null, null, null, null, COL_HORA + " ASC");
        if (c != null && c.moveToFirst()) {
            do { lista.add(cursorToTask(c)); } while (c.moveToNext());
            c.close();
        }
        db.close();
        return lista;
    }

    public int marcarCompletada(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_COMPLETADA, 1);
        int filas = db.update(TABLE_TAREAS, v, COL_ID + " = ?",
                new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    public int eliminarTarea(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLE_TAREAS, COL_ID + " = ?",
                new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    private TaskModel cursorToTask(Cursor c) {
        return new TaskModel(
                c.getInt   (c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_DESCRIPCION)),
                c.getString(c.getColumnIndexOrThrow(COL_FECHA_LIMITE)),
                c.getString(c.getColumnIndexOrThrow(COL_PRIORIDAD)),
                c.getInt   (c.getColumnIndexOrThrow(COL_COMPLETADA))      == 1,
                c.getInt   (c.getColumnIndexOrThrow(COL_AUTO_PROGRAMADA)) == 1,
                c.getString(c.getColumnIndexOrThrow(COL_HORA))
        );
    }

    // ===================================================================
    // USUARIOS — CRUD
    // ===================================================================

    public long insertarUsuario(User usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_USR_NOMBRE,   usuario.getNombre());
        v.put(COL_USR_EMAIL,    usuario.getEmail());
        v.put(COL_USR_PASSWORD, usuario.getPasswordHash());
        v.put(COL_USR_ROL,      usuario.getRol());
        v.put(COL_USR_FECHA,    usuario.getFechaRegistro());
        long id = db.insert(TABLE_USUARIOS, null, v);
        db.close();
        return id;
    }

    public User obtenerUsuarioPorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USUARIOS, null,
                COL_USR_EMAIL + " = ?", new String[]{email},
                null, null, null);
        User usuario = null;
        if (c != null && c.moveToFirst()) {
            usuario = cursorToUser(c);
            c.close();
        }
        db.close();
        return usuario;
    }

    public boolean emailExiste(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USUARIOS, new String[]{COL_USR_ID},
                COL_USR_EMAIL + " = ?", new String[]{email},
                null, null, null);
        boolean existe = (c != null && c.getCount() > 0);
        if (c != null) c.close();
        db.close();
        return existe;
    }

    private User cursorToUser(Cursor c) {
        return new User(
                c.getInt   (c.getColumnIndexOrThrow(COL_USR_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_USR_NOMBRE)),
                c.getString(c.getColumnIndexOrThrow(COL_USR_EMAIL)),
                c.getString(c.getColumnIndexOrThrow(COL_USR_PASSWORD)),
                c.getString(c.getColumnIndexOrThrow(COL_USR_ROL)),
                c.getString(c.getColumnIndexOrThrow(COL_USR_FECHA))
        );
    }

    // ===================================================================
    // SUBTAREAS — CRUD
    // ===================================================================

    public long insertarSubtarea(Subtarea subtarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_SUB_TAREA_ID,    subtarea.getTareaId());
        v.put(COL_SUB_DESCRIPCION, subtarea.getDescripcion());
        v.put(COL_SUB_COMPLETADA,  subtarea.isCompletada() ? 1 : 0);
        long id = db.insert(TABLE_SUBTAREAS, null, v);
        db.close();
        return id;
    }

    public List<Subtarea> obtenerSubtareasDeTarea(int tareaId) {
        List<Subtarea> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_SUBTAREAS, null,
                COL_SUB_TAREA_ID + " = ?", new String[]{String.valueOf(tareaId)},
                null, null, COL_SUB_ID + " ASC");
        if (c != null && c.moveToFirst()) {
            do { lista.add(cursorToSubtarea(c)); } while (c.moveToNext());
            c.close();
        }
        db.close();
        return lista;
    }

    public int marcarSubtareaCompletada(int subtareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_SUB_COMPLETADA, 1);
        int filas = db.update(TABLE_SUBTAREAS, v, COL_SUB_ID + " = ?",
                new String[]{String.valueOf(subtareaId)});
        db.close();
        return filas;
    }

    public boolean todasCompletadas(int tareaId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Cuenta las subtareas que NO están completadas
        Cursor c = db.query(TABLE_SUBTAREAS,
                new String[]{"COUNT(*)"},
                COL_SUB_TAREA_ID + " = ? AND " + COL_SUB_COMPLETADA + " = 0",
                new String[]{String.valueOf(tareaId)},
                null, null, null);
        boolean todas = false;
        if (c != null && c.moveToFirst()) {
            int pendientes = c.getInt(0);
            // Verificamos también que haya al menos una subtarea
            c.close();
            Cursor cTotal = db.query(TABLE_SUBTAREAS,
                    new String[]{"COUNT(*)"},
                    COL_SUB_TAREA_ID + " = ?",
                    new String[]{String.valueOf(tareaId)},
                    null, null, null);
            if (cTotal != null && cTotal.moveToFirst()) {
                int total = cTotal.getInt(0);
                todas = (total > 0 && pendientes == 0);
                cTotal.close();
            }
        }
        db.close();
        return todas;
    }

    private Subtarea cursorToSubtarea(Cursor c) {
        return new Subtarea(
                c.getInt   (c.getColumnIndexOrThrow(COL_SUB_ID)),
                c.getInt   (c.getColumnIndexOrThrow(COL_SUB_TAREA_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_SUB_DESCRIPCION)),
                c.getInt   (c.getColumnIndexOrThrow(COL_SUB_COMPLETADA)) == 1
        );
    }
}
