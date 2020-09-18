package com.emarsys.sample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.inapp.ui.InlineInAppView
import com.emarsys.sample.R
import com.emarsys.sample.extensions.showSnackBar
import kotlinx.android.synthetic.main.fragment_inline_in_app.*

class InlineInAppFragment : Fragment() {
    companion object {
        val TAG: String = InlineInAppFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inline_in_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inlineInAppFullyFromXml.onCloseListener = {
            inlineInAppFullyFromXml.visibility = View.GONE
        }

        list.adapter = Adapter()
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
    }
}