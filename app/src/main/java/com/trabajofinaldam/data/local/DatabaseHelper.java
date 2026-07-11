package com.trabajofinaldam.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.model.TaskModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "ecotask.db";
    private static final int    DB_VERSION = 6;

    public static final String TABLE_TAREAS        = "tareas";
    public static final String COL_ID              = "id";
    public static final String COL_TITULO          = "titulo";
    public static final String COL_DESCRIPCION     = "descripcion";
    public static final String COL_FECHA_LIMITE    = "fecha_limite";
    public static final String COL_PRIORIDAD       = "prioridad";
    public static final String COL_COMPLETADA      = "completada";
    public static final String COL_AUTO_PROGRAMADA = "auto_programada";
    public static final String COL_HORA            = "hora";
    public static final String COL_INICIADA        = "iniciada";
    public static final String COL_FECHA_COMPLETADA = "fecha_completada";

    public static final String TABLE_SUBTAREAS     = "subtareas";
    public static final String COL_SUB_ID          = "id";
    public static final String COL_SUB_TAREA_ID    = "tarea_id";
    public static final String COL_SUB_DESCRIPCION = "descripcion";
    public static final String COL_SUB_COMPLETADA  = "completada";

    public static final String TABLE_USUARIOS      = "usuarios";
    public static final String COL_USER_ID         = "id";
    public static final String COL_USER_NAME       = "nombre";
    public static final String COL_USER_EMAIL      = "email";
    public static final String COL_USER_PASS       = "password";

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
        String createTableTareas =
                "CREATE TABLE " + TABLE_TAREAS + " (" +
                        COL_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_TITULO          + " TEXT    NOT NULL, "                  +
                        COL_DESCRIPCION     + " TEXT    NOT NULL, "                  +
                        COL_FECHA_LIMITE    + " TEXT    NOT NULL, "                  +
                        COL_PRIORIDAD       + " TEXT    NOT NULL DEFAULT 'MEDIA', "  +
                        COL_COMPLETADA      + " INTEGER NOT NULL DEFAULT 0, "        +
                        COL_AUTO_PROGRAMADA + " INTEGER NOT NULL DEFAULT 0, "        +
                        COL_HORA            + " TEXT    NOT NULL DEFAULT '', "       +
                        COL_INICIADA        + " INTEGER NOT NULL DEFAULT 0, "         +
                        COL_FECHA_COMPLETADA + " TEXT"                                +
                        ");";

        String createTableSubtareas = "CREATE TABLE " + TABLE_SUBTAREAS + " (" +
                COL_SUB_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SUB_TAREA_ID    + " INTEGER NOT NULL, " +
                COL_SUB_DESCRIPCION + " TEXT NOT NULL, " +
                COL_SUB_COMPLETADA  + " INTEGER DEFAULT 0" +
                ");";

        String createTableUsuarios = "CREATE TABLE " + TABLE_USUARIOS + " (" +
                COL_USER_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME  + " TEXT NOT NULL, " +
                COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASS  + " TEXT NOT NULL" +
                ");";

        db.execSQL(createTableTareas);
        db.execSQL(createTableSubtareas);
        db.execSQL(createTableUsuarios);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTAREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    public long registrarUsuario(String nombre, String email, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, nombre);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASS, pass);
        long id = db.insert(TABLE_USUARIOS, null, values);
        db.close();
        return id;
    }

    public boolean validarLogin(String email, String pass) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, null,
                COL_USER_EMAIL + "=? AND " + COL_USER_PASS + "=?",
                new String[]{email, pass}, null, null, null);
        boolean valid = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return valid;
    }

    public long insertarTarea(TaskModel tarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITULO,          tarea.getTitulo());
        values.put(COL_DESCRIPCION,     tarea.getDescripcion());
        values.put(COL_FECHA_LIMITE,    tarea.getFechaLimite());
        values.put(COL_PRIORIDAD,       tarea.getPrioridad());
        values.put(COL_COMPLETADA,      tarea.isCompletada()     ? 1 : 0);
        values.put(COL_AUTO_PROGRAMADA, tarea.isAutoProgramada() ? 1 : 0);
        values.put(COL_HORA,            tarea.getHora());
        values.put(COL_INICIADA,        tarea.isIniciada()       ? 1 : 0);
        long id = db.insert(TABLE_TAREAS, null, values);
        db.close();
        return id;
    }

    public long insertarSubtarea(Subtarea subtarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SUB_TAREA_ID,    subtarea.getTareaId());
        values.put(COL_SUB_DESCRIPCION, subtarea.getDescripcion());
        values.put(COL_SUB_COMPLETADA,  subtarea.isCompletada() ? 1 : 0);
        long id = db.insert(TABLE_SUBTAREAS, null, values);
        db.close();
        return id;
    }

    public List<TaskModel> obtenerTareasPendientes() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.*, " +
                "(SELECT COUNT(*) FROM " + TABLE_SUBTAREAS + " WHERE " + COL_SUB_TAREA_ID + " = t." + COL_ID + ") as total_sub, " +
                "(SELECT COUNT(*) FROM " + TABLE_SUBTAREAS + " WHERE " + COL_SUB_TAREA_ID + " = t." + COL_ID + " AND " + COL_SUB_COMPLETADA + " = 1) as comp_sub " +
                "FROM " + TABLE_TAREAS + " t " +
                "WHERE t." + COL_COMPLETADA + " = 0 " +
                "ORDER BY CASE " + COL_PRIORIDAD + " " +
                "WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 WHEN 'BAJA' THEN 3 ELSE 4 END, " +
                "t." + COL_HORA + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do { lista.add(cursorToTask(cursor)); } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return lista;
    }

    public List<TaskModel> obtenerTareasFinalizadas() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.*, " +
                "(SELECT COUNT(*) FROM " + TABLE_SUBTAREAS + " WHERE " + COL_SUB_TAREA_ID + " = t." + COL_ID + ") as total_sub, " +
                "(SELECT COUNT(*) FROM " + TABLE_SUBTAREAS + " WHERE " + COL_SUB_TAREA_ID + " = t." + COL_ID + " AND " + COL_SUB_COMPLETADA + " = 1) as comp_sub " +
                "FROM " + TABLE_TAREAS + " t " +
                "WHERE t." + COL_COMPLETADA + " = 1 " +
                "ORDER BY t." + COL_FECHA_COMPLETADA + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do { lista.add(cursorToTask(cursor)); } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return lista;
    }

    public List<TaskModel> obtenerTodasLasTareas() {
        List<TaskModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.*, " +
                "(SELECT COUNT(*) FROM " + TABLE_SUBTAREAS + " WHERE " + COL_SUB_TAREA_ID + " = t." + COL_ID + ") as total_sub, " +
                "(SELECT COUNT(*) FROM " + TABLE_SUBTAREAS + " WHERE " + COL_SUB_TAREA_ID + " = t." + COL_ID + " AND " + COL_SUB_COMPLETADA + " = 1) as comp_sub " +
                "FROM " + TABLE_TAREAS + " t " +
                "ORDER BY CASE " + COL_PRIORIDAD + " " +
                "WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 WHEN 'BAJA' THEN 3 ELSE 4 END, " +
                "t." + COL_HORA + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do { lista.add(cursorToTask(cursor)); } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return lista;
    }

    public List<Subtarea> obtenerSubtareasDeTarea(int tareaId) {
        List<Subtarea> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_SUBTAREAS, null, COL_SUB_TAREA_ID + " = ?", new String[]{String.valueOf(tareaId)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                lista.add(new Subtarea(
                        c.getInt(c.getColumnIndexOrThrow(COL_SUB_ID)),
                        c.getInt(c.getColumnIndexOrThrow(COL_SUB_TAREA_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_SUB_DESCRIPCION)),
                        c.getInt(c.getColumnIndexOrThrow(COL_SUB_COMPLETADA)) == 1
                ));
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return lista;
    }

    public int actualizarEstadoSubtarea(int subtareaId, boolean completada) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SUB_COMPLETADA, completada ? 1 : 0);
        int filas = db.update(TABLE_SUBTAREAS, values, COL_SUB_ID + " = ?", new String[]{String.valueOf(subtareaId)});
        db.close();
        return filas;
    }

    public int marcarCompletada(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLETADA, 1);
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        values.put(COL_FECHA_COMPLETADA, fechaHoy);
        int filas = db.update(TABLE_TAREAS, values, COL_ID + " = ?", new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    public int desmarcarTarea(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLETADA, 0);
        values.put(COL_FECHA_COMPLETADA, (String) null);
        int filas = db.update(TABLE_TAREAS, values, COL_ID + " = ?", new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    public int postergarTarea(int tareaId, String nuevaFecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_FECHA_LIMITE, nuevaFecha);
        int filas = db.update(TABLE_TAREAS, v, COL_ID + " = ?", new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    public int marcarIniciada(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_INICIADA, 1);
        int filas = db.update(TABLE_TAREAS, values, COL_ID + " = ?", new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    public int actualizarTarea(TaskModel tarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITULO,          tarea.getTitulo());
        values.put(COL_DESCRIPCION,     tarea.getDescripcion());
        values.put(COL_FECHA_LIMITE,    tarea.getFechaLimite());
        values.put(COL_HORA,            tarea.getHora());
        values.put(COL_PRIORIDAD,       tarea.getPrioridad());
        int filas = db.update(TABLE_TAREAS, values, COL_ID + " = ?", new String[]{String.valueOf(tarea.getId())});
        db.close();
        return filas;
    }

    public int eliminarSubtarea(int subtareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLE_SUBTAREAS, COL_SUB_ID + " = ?", new String[]{String.valueOf(subtareaId)});
        db.close();
        return filas;
    }

    public void borrarSubtareasDeTarea(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBTAREAS, COL_SUB_TAREA_ID + " = ?", new String[]{String.valueOf(tareaId)});
        db.close();
    }

    public int eliminarTarea(int tareaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLE_TAREAS, COL_ID + " = ?", new String[]{String.valueOf(tareaId)});
        db.close();
        return filas;
    }

    private TaskModel cursorToTask(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String tit = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
        String fec = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_LIMITE));
        String pri = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIORIDAD));
        boolean comp = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETADA)) == 1;
        boolean auto = cursor.getInt(cursor.getColumnIndexOrThrow(COL_AUTO_PROGRAMADA)) == 1;
        String hor = cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA));
        boolean ini = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INICIADA)) == 1;
        
        int totalSub = 0;
        int compSub = 0;
        int idxTotal = cursor.getColumnIndex("total_sub");
        int idxComp = cursor.getColumnIndex("comp_sub");
        if (idxTotal != -1) totalSub = cursor.getInt(idxTotal);
        if (idxComp != -1) compSub = cursor.getInt(idxComp);

        String fechaComp = null;
        int idxFechaComp = cursor.getColumnIndex(COL_FECHA_COMPLETADA);
        if (idxFechaComp != -1) fechaComp = cursor.getString(idxFechaComp);

        return new TaskModel(id, tit, desc, fec, pri, comp, auto, hor, ini, totalSub, compSub, fechaComp);
    }
}
