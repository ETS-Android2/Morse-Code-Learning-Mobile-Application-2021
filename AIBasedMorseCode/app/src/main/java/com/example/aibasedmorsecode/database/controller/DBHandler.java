package com.example.aibasedmorsecode.database.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.aibasedmorsecode.model.Category;
import com.example.aibasedmorsecode.model.MultiUserSession;
import com.example.aibasedmorsecode.model.Question;
import com.example.aibasedmorsecode.model.Receiver;
import com.example.aibasedmorsecode.database.utility.CategoryUtil;
import com.example.aibasedmorsecode.database.utility.DBUtil;
import com.example.aibasedmorsecode.database.utility.MultiUserUtil;
import com.example.aibasedmorsecode.database.utility.QuestionUtil;
import com.example.aibasedmorsecode.database.utility.ReceiverUtil;

public class DBHandler extends SQLiteOpenHelper {

    public DBHandler(@Nullable Context context) {
        super(context, DBUtil.DATABASE_NAME, null, DBUtil.DATABASE_VERSION);
    }

    /**
     * This method creates the tables in the database
     * @param db as SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryUtil.TABLE_NAME +
                " (" + CategoryUtil.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CategoryUtil.KEY_CATEGORY + " TEXT" +
                " )";
        db.execSQL(CREATE_CATEGORY_TABLE);

        String CREATE_MULTIUSERSESSION_TABLE = "CREATE TABLE " + MultiUserUtil.TABLE_NAME +
                " (" + MultiUserUtil.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MultiUserUtil.KEY_SESSION_NAME + " TEXT," +
                MultiUserUtil.KEY_LEARNING_TYPE + " TEXT," +
                MultiUserUtil.KEY_NUMBER_ROUNDS + " INTEGER," +
                MultiUserUtil.KEY_DATE + " TEXT," +
                MultiUserUtil.KEY_SENDER_NAME + " TEXT," +
                MultiUserUtil.FKEY_CATEGORY_ID + " INTEGER," +
                " CONSTRAINT \"FK1\" FOREIGN KEY(" + MultiUserUtil.FKEY_CATEGORY_ID + ") REFERENCES "
                + CategoryUtil.TABLE_NAME + "(" + CategoryUtil.KEY_ID + ")" +
                " )";
        db.execSQL(CREATE_MULTIUSERSESSION_TABLE);

        String CREATE_RECEIVER_TABLE = "CREATE TABLE " + ReceiverUtil.TABLE_NAME +
                " (" + ReceiverUtil.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReceiverUtil.KEY_RECEIVER_NAME + " TEXT," +
                ReceiverUtil.KEY_POINT + " INTEGER," +
                ReceiverUtil.FKEY_SESSION_ID + " INTEGER," +
                " CONSTRAINT \"FK1\" FOREIGN KEY(" + ReceiverUtil.FKEY_SESSION_ID + ") REFERENCES "
                + MultiUserUtil.TABLE_NAME + "(" + MultiUserUtil.KEY_ID + ")" +
                " )";
        db.execSQL(CREATE_RECEIVER_TABLE);

        String CREATE_QUESTION_TABLE = "CREATE TABLE " + QuestionUtil.TABLE_NAME +
                " (" + QuestionUtil.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                QuestionUtil.KEY_MESSAGE + " TEXT," +
                QuestionUtil.FKEY_CATEGORY_ID + " INTEGER," +
                " CONSTRAINT \"FK1\" FOREIGN KEY(" + QuestionUtil.FKEY_CATEGORY_ID + ") REFERENCES "
                + CategoryUtil.TABLE_NAME + "(" + CategoryUtil.KEY_ID + ")" +
                " )";
        db.execSQL(CREATE_QUESTION_TABLE);
    }

    /**
     * method that will be used to upgrade the database; if the database is newer
     * @param db as SQLiteDatabase
     * @param oldVersion as int
     * @param newVersion as int
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ReceiverUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MultiUserUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryUtil.TABLE_NAME);
        onCreate(db);
    }

    /**
     * This method is used to reinitialize the database
     */
    public void init(){
        if(getAllCategory().size() != 0) {return;}
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + CategoryUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MultiUserUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReceiverUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionUtil.TABLE_NAME);
        db.execSQL("PRAGMA foreign_keys = ON;");
        onCreate(db);
        this.addQuestionSet();
    }
    //---------------------------------------------------Category Table Methods-----------------------------------------------

    /**
     * Method for adding a Category to the database
     * @param category as Category
     */
    public void addCategory(Category category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoryUtil.KEY_CATEGORY, category.getCategoryName());
        db.insert(CategoryUtil.TABLE_NAME, null, contentValues);
        db.close();
    }


    /**
     * Method for retrieving a Category from the database
     * @param id as itn
     * @return category as Category
     */
    public Category getCategory(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CategoryUtil.TABLE_NAME,
                new String[]{CategoryUtil.KEY_ID, CategoryUtil.KEY_CATEGORY},
                CategoryUtil.KEY_ID + "=?",new String[]{String.valueOf(id)},
                null,null,null);
        if (cursor.moveToFirst()){
            /*cursor.moveToFirst();*/
            Category category = new Category(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1));
            return category;
        }
        return null;
    }

    /**
     * Method for retrieving a Category from the database
     * @param categoryName as String
     * @return category as Category
     */
    public Category getCategory(String categoryName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CategoryUtil.TABLE_NAME,
                new String[]{CategoryUtil.KEY_ID, CategoryUtil.KEY_CATEGORY},
                CategoryUtil.KEY_CATEGORY + "=?",new String[]{String.valueOf(categoryName)},
                null,null,null);
        if (cursor.moveToFirst()){
            Category category = new Category(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1));
            category.setQuestions(getAllQuestion(Integer.parseInt(cursor.getString(0))));
            return category;
        }
        return null;
    }

    /**
     * Method for retrieving all Categories from the database
     * @return categoryList as List<Category>
     */
    public List<Category> getAllCategory(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Category> categoryList = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + CategoryUtil.TABLE_NAME;
        Cursor cursor = db.rawQuery(getAllQuery,null);
        if (cursor.moveToFirst()){
            do {
                Category category = new Category(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1));
                categoryList.add(category);
            }while(cursor.moveToNext());
        }
        return categoryList;
    }


    /**
     * Method for updating a Category in the database
     * @param category as Category
     */
    public void updateCategory(Category category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoryUtil.KEY_CATEGORY, category.getCategoryName());
        db.update(CategoryUtil.TABLE_NAME,contentValues,CategoryUtil.KEY_ID + "=?", new String[] {String.valueOf(category.getId())});
        db.close();
    }

    /**
     * Method for removing a Category from the database
     * @param id as int
     */
    public void removeCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(CategoryUtil.TABLE_NAME, CategoryUtil.KEY_ID +"=?",new String[]{String.valueOf(id)});
        db.close();

    };
    //---------------------------------------------------MultiUserSession Table Methods-----------------------------------------------

    /**
     * Method for adding a MuliUserSession to the database
     * @param multiUserSession as MultiUserSession
     */
    public void addMultiUserSession(MultiUserSession multiUserSession){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MultiUserUtil.KEY_SESSION_NAME, multiUserSession.getSessionName());
        contentValues.put(MultiUserUtil.KEY_LEARNING_TYPE, multiUserSession.getLearningType());
        contentValues.put(MultiUserUtil.KEY_NUMBER_ROUNDS, multiUserSession.getNumRounds());
        contentValues.put(MultiUserUtil.KEY_DATE, new SimpleDateFormat("YYYY-MM-DD HH:MM:SS.").format(multiUserSession.getDate()));
        contentValues.put(MultiUserUtil.KEY_SENDER_NAME, multiUserSession.getSenderName());
        contentValues.put(MultiUserUtil.FKEY_CATEGORY_ID, multiUserSession.getCategoryid());
        db.insert(MultiUserUtil.TABLE_NAME, null, contentValues);
        db.close();

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX("+MultiUserUtil.KEY_ID+") FROM " +MultiUserUtil.TABLE_NAME,null);
        cursor.moveToFirst();
        long sessionid = cursor.getInt(0);
        db.close();

        for(Receiver r:multiUserSession.getReceivers()){
            r.setSessionID((int)sessionid);
            addReceiver(r);
        }
    }


    /**
     * Method for retrieving a MultiUserSession from the database
     * @param id as int
     * @return multiUserSession as MultiUserSession
     */
    public MultiUserSession getMuliUserSession(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MultiUserUtil.TABLE_NAME,
                new String[]{MultiUserUtil.KEY_ID, MultiUserUtil.KEY_SESSION_NAME, MultiUserUtil.KEY_LEARNING_TYPE,
                        MultiUserUtil.KEY_NUMBER_ROUNDS, MultiUserUtil.KEY_DATE, MultiUserUtil.KEY_SENDER_NAME, MultiUserUtil.FKEY_CATEGORY_ID},
                MultiUserUtil.KEY_ID + "=?",new String[]{String.valueOf(id)},
                null,null,null);
        if (cursor != null){
            try {
                cursor.moveToFirst();
                MultiUserSession multiUserSession = new MultiUserSession(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        Integer.parseInt(cursor.getString(3)),
                        new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.").parse(cursor.getString(4)),
                        cursor.getString(5),
                        Integer.parseInt(((cursor.getString(6)) == null) ? "0" : cursor.getString(6)));
                return multiUserSession;
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Method for retrieving all the MultiUserSessions from the database
     * @return multiUserSessionList ArrayList<MultiUserSession>
     */
    public ArrayList<MultiUserSession> getAllMultiUserSessions(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MultiUserSession> multiUserSessionList = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + MultiUserUtil.TABLE_NAME;
        Cursor cursor = db.rawQuery(getAllQuery,null);
        if (cursor.moveToFirst()){
            try {
                do {
                    MultiUserSession multiUserSession = new MultiUserSession(Integer.parseInt(cursor.getString(0)),
                            cursor.getString(1),
                            cursor.getString(2),
                            Integer.parseInt(cursor.getString(3)),
                            new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.").parse(cursor.getString(4)),
                            cursor.getString(5),
                            Integer.parseInt(((cursor.getString(6)) == null) ? "0" : cursor.getString(6)));
                    if((multiUserSession.getLearningType()).equals("Training")){ // only in Training mode
                        multiUserSession.setCategoryName(getCategory(multiUserSession.getCategoryid()).getCategoryName());
                    }
                    multiUserSession.setReceivers(getSessionReceiver(multiUserSession.getId()));
                    multiUserSessionList.add(multiUserSession);
                } while (cursor.moveToNext());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }

        }
        return multiUserSessionList;
    }


    /**
     * Method for updating a MultiUserSession in the database
     * @param multiUserSession as MultiUserSession
     */
    public void updateMultiUserSession(MultiUserSession multiUserSession){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MultiUserUtil.KEY_SESSION_NAME, multiUserSession.getSessionName());
        contentValues.put(MultiUserUtil.KEY_LEARNING_TYPE, multiUserSession.getLearningType());
        contentValues.put(MultiUserUtil.KEY_NUMBER_ROUNDS, multiUserSession.getNumRounds());
        contentValues.put(MultiUserUtil.KEY_DATE, new SimpleDateFormat("YYYY-MM-DD HH:MM:SS.").format(multiUserSession.getDate()));
        contentValues.put(MultiUserUtil.KEY_SENDER_NAME, multiUserSession.getSenderName());
        contentValues.put(MultiUserUtil.FKEY_CATEGORY_ID, multiUserSession.getCategoryid());
        db.update(MultiUserUtil.TABLE_NAME, contentValues, MultiUserUtil.KEY_ID + "=?", new String[]{String.valueOf(multiUserSession.getId())});
        db.close();
    }


    /**
     * Method for removing a MultiUserSession from the database
     * @param id as int
     */
    public void removeMultiUserSession(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(MultiUserUtil.TABLE_NAME, MultiUserUtil.KEY_ID +"=?",new String[]{String.valueOf(id)});
        db.close();
    };
    //---------------------------------------------------Question Table Methods-----------------------------------------------

    /**
     * Method for adding a Question to the database
     * @param question as Question
     */
    public void addQuestion(Question question){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QuestionUtil.KEY_MESSAGE, question.getMessage());
        contentValues.put(QuestionUtil.FKEY_CATEGORY_ID, question.getCategoryID());
        db.insert(QuestionUtil.TABLE_NAME, null, contentValues);
        db.close();
    }


    /**
     * Method for retrieving a Question from the database
     * @param id as int
     * @return question as Question
     */
    public Question getQuestion(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(QuestionUtil.TABLE_NAME,
                new String[]{QuestionUtil.KEY_ID, QuestionUtil.KEY_MESSAGE, QuestionUtil.FKEY_CATEGORY_ID},
                QuestionUtil.KEY_ID + "=?",new String[]{String.valueOf(id)},
                null,null,null);
        if (cursor != null){
            cursor.moveToFirst();
            Question question = new Question(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    Integer.parseInt(((cursor.getString(2)) == null) ? "0" : cursor.getString(2)));
            return question;
        }
        return null;
    }


    /**
     * Method for retrieving all Questions from the database
     * @return questionList as ist<Question>
     */
    public List<Question> getAllQuestion(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Question> questionList = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + QuestionUtil.TABLE_NAME;
        Cursor cursor = db.rawQuery(getAllQuery,null);
        if (cursor.moveToFirst()){
            do {
                Question question = new Question(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        Integer.parseInt(((cursor.getString(2)) == null) ? "0" : cursor.getString(2)));
                questionList.add(question);
            }while(cursor.moveToNext());
        }
        return questionList;
    }

    /**
     * Method for retrieving all Questions from the database
     * @param categoryid as int
     * @return questionList as ArrayList<Question>
     */
    public ArrayList<Question> getAllQuestion(int categoryid){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Question> questionList = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + QuestionUtil.TABLE_NAME;

        Cursor cursor = db.query(QuestionUtil.TABLE_NAME,
                new String[]{QuestionUtil.KEY_ID, QuestionUtil.KEY_MESSAGE, QuestionUtil.FKEY_CATEGORY_ID},
                QuestionUtil.FKEY_CATEGORY_ID + "=?",new String[]{String.valueOf(categoryid)},
                null,null,null);
        if (cursor.moveToFirst()){
            do {
                Question question = new Question(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        Integer.parseInt(((cursor.getString(2)) == null) ? "0" : cursor.getString(2)));
                questionList.add(question);
            }while(cursor.moveToNext());
        }
        return questionList;
    }

    /**
     * Method for updating a Question in the database
     * @param question as Question
     */
    public void updateQuestion(Question question){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QuestionUtil.KEY_MESSAGE, question.getMessage());
        contentValues.put(QuestionUtil.FKEY_CATEGORY_ID, question.getCategoryID());
        db.update(QuestionUtil.TABLE_NAME, contentValues, QuestionUtil.KEY_ID + "=?", new String[]{String.valueOf(question.getId())});
        db.close();
    }

    /**
     * Method for removing a Question from the database
     * @param id as int
     */
    public void removeQuestion(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(QuestionUtil.TABLE_NAME, QuestionUtil.KEY_ID +"=?",new String[]{String.valueOf(id)});
        db.close();
    };
    //---------------------------------------------------Receiver Table Methods-----------------------------------------------

    /**
     * Method for adding a Receiver to the database
     * @param receiver as Receiver
     */
    public void addReceiver(Receiver receiver){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReceiverUtil.KEY_RECEIVER_NAME, receiver.getUsername());
        contentValues.put(ReceiverUtil.KEY_POINT, receiver.getPoints());
        contentValues.put(ReceiverUtil.FKEY_SESSION_ID, receiver.getSessionID());
        db.insert(ReceiverUtil.TABLE_NAME, null, contentValues);
        db.close();
    }


    /**
     * Method for retrieving a Receiver from the database
     * @param id as int
     * @return receiver as Receiver
     */
    public Receiver getReceiver(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ReceiverUtil.TABLE_NAME,
                new String[]{ReceiverUtil.KEY_ID, ReceiverUtil.KEY_RECEIVER_NAME, ReceiverUtil.KEY_POINT},
                ReceiverUtil.KEY_ID + "=?",new String[]{String.valueOf(id)},
                null,null,null);
        if (cursor.moveToFirst()){
            Receiver receiver = new Receiver(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    Integer.parseInt(cursor.getString(2)),
                    Integer.parseInt(((cursor.getString(3)) == null) ? "0" : cursor.getString(3)));
            return receiver;
        }
        return null;
    }


    /**
     * Method for retrieving all Receivers from the database
     * @return receiverList as ArrayList<Receiver>
     */
    public ArrayList<Receiver> getSessionReceiver(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Receiver> receiverList = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + ReceiverUtil.TABLE_NAME;
        Cursor cursor = db.rawQuery(getAllQuery,null);
        if (cursor.moveToFirst()){
            do {
                Receiver receiver = new Receiver(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        Integer.parseInt(cursor.getString(2)),
                        Integer.parseInt(((cursor.getString(3)) == null) ? "0" : cursor.getString(3)));
                receiverList.add(receiver);
            }while(cursor.moveToNext());

        }
        return receiverList;
    }


    /**
     * Method for retrieving Receivers of a session from the database
     * @param sessionid as int
     * @return receiverList as ArrayList<Receiver>
     */
    public ArrayList<Receiver> getSessionReceiver(int sessionid){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Receiver> receiverList = new ArrayList<>();
        Cursor cursor = db.query(ReceiverUtil.TABLE_NAME,
                new String[]{ReceiverUtil.KEY_ID, ReceiverUtil.KEY_RECEIVER_NAME, ReceiverUtil.KEY_POINT, ReceiverUtil.FKEY_SESSION_ID},
                ReceiverUtil.FKEY_SESSION_ID + "=?",new String[]{String.valueOf(sessionid)},
                null,null,null);
        while(cursor.moveToNext()) {
            Receiver receiver = new Receiver(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    Integer.parseInt(cursor.getString(2)),
                    Integer.parseInt(((cursor.getString(3)) == null) ? "0" : cursor.getString(3)));
            receiverList.add(receiver);
        }
        return receiverList;
    }


    /**
     * Method for updating a Receiver in the database
     * @param receiver as Receiver
     */
    public void updateReceiver(Receiver receiver){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReceiverUtil.KEY_RECEIVER_NAME, receiver.getUsername());
        contentValues.put(ReceiverUtil.KEY_POINT, receiver.getPoints());
        contentValues.put(ReceiverUtil.FKEY_SESSION_ID, receiver.getSessionID());
        db.update(ReceiverUtil.TABLE_NAME, contentValues, ReceiverUtil.KEY_ID + "=?", new String[]{String.valueOf(receiver.getId())});
        db.close();
    }


    /**
     * Method for removing a Receiver from the database
     * @param id as int
     */
    public void removeReceiver(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ReceiverUtil.TABLE_NAME, ReceiverUtil.KEY_ID +"=?",new String[]{String.valueOf(id)});
        db.close();
    };

    /**
     * To add pre-defined questions to the database
     */
    public void addQuestionSet(){
        this.addCategory(new Category("Letter"));
        this.addCategory(new Category("Numbers"));
        this.addCategory(new Category("Punctuations"));
        this.addQuestion(new Question("A",1));
        this.addQuestion(new Question("B",1));
        this.addQuestion(new Question("C",1));
        this.addQuestion(new Question("D",1));
        this.addQuestion(new Question("E",1));
        this.addQuestion(new Question("F",1));
        this.addQuestion(new Question("G",1));
        this.addQuestion(new Question("H",1));
        this.addQuestion(new Question("I",1));
        this.addQuestion(new Question("J",1));
        this.addQuestion(new Question("K",1));
        this.addQuestion(new Question("L",1));
        this.addQuestion(new Question("M",1));
        this.addQuestion(new Question("N",1));
        this.addQuestion(new Question("O",1));
        this.addQuestion(new Question("P",1));
        this.addQuestion(new Question("Q",1));
        this.addQuestion(new Question("R",1));
        this.addQuestion(new Question("S",1));
        this.addQuestion(new Question("T",1));
        this.addQuestion(new Question("U",1));
        this.addQuestion(new Question("V",1));
        this.addQuestion(new Question("W",1));
        this.addQuestion(new Question("X",1));
        this.addQuestion(new Question("Y",1));
        this.addQuestion(new Question("Z",1));
        this.addQuestion(new Question("0",2));
        this.addQuestion(new Question("1",2));
        this.addQuestion(new Question("2",2));
        this.addQuestion(new Question("3",2));
        this.addQuestion(new Question("4",2));
        this.addQuestion(new Question("5",2));
        this.addQuestion(new Question("6",2));
        this.addQuestion(new Question("7",2));
        this.addQuestion(new Question("8",2));
        this.addQuestion(new Question("9",2));
        this.addQuestion(new Question(".",3));
        this.addQuestion(new Question(",",3));
        this.addQuestion(new Question("?",3));
        this.addQuestion(new Question("\'",3));
        this.addQuestion(new Question("!",3));
        this.addQuestion(new Question("/",3));
        this.addQuestion(new Question("&",3));
        this.addQuestion(new Question(":",3));
        this.addQuestion(new Question(";",3));
        this.addQuestion(new Question("=",3));
        this.addQuestion(new Question("+",3));
        this.addQuestion(new Question("-",3));
        this.addQuestion(new Question("_",3));
        this.addQuestion(new Question("\"",3));
        this.addQuestion(new Question("$",3));
        this.addQuestion(new Question("@",3));
    }
}
