package com.example.taller1_migueluribe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.taller1_migueluribe.CountriesActivity.Country

// Acá lo que hice fue definir un adaptador CountryAdapter que infla la lista simple_list_item_1
// para cada pais de la lista estableciendo a la vez el TextView.
class CountryAdapter(context: Context, resource: Int, objects: List<Country>) :
    ArrayAdapter<Country>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        val country = getItem(position)
        val textView = rowView.findViewById<TextView>(android.R.id.text1)
        textView.text = country?.nombre_pais
        return rowView
    }
}
