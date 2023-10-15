package com.example.migueluribe_taller2.adapters

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.migueluribe_taller2.R

// Este es el adaptador para que aparezcan conrrectamente los contactos de el usuario en cuestiòn correctamente con respecto
// al contactrow.xml y activity_contacts.xml
class ContactsAdapter(context: Context?, c: Cursor?, flags: Int) :
    CursorAdapter(context, c, flags) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.contactrow, parent, false)
    }
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val tvId = view!!.findViewById<TextView>(R.id.contactId)
        val tvName = view!!.findViewById<TextView>(R.id.contactName)
        val id = cursor!!.getInt(0)
        val name = cursor!!.getString(1)
        tvId.text=id.toString()
        tvName.text=name
    }
}