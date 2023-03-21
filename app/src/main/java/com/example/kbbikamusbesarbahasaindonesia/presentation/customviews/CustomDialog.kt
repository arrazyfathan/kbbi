package com.example.kbbikamusbesarbahasaindonesia.presentation.customviews

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.databinding.CustomDialogBinding

/**
 * Created by Ar Razy Fathan Rabbani on 21/03/23.
 */
class CustomDialog private constructor(
    context: Context,
    private val title: String?,
    private val message: String?,
    private val okTitle: String?,
    private val cancelTitle: String?,
    private val isCancelable: Boolean?,
    val listener: (ResponseType) -> Unit,
) : Dialog(context) {

    private lateinit var binding: CustomDialogBinding
    private lateinit var dialog: Dialog

    enum class ResponseType {
        YES, NO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = this
        setupWindow()
        setupView()
        isCancelable?.let {
            this.setCancelable(it)
        }
    }

    private fun setupView() = with(binding) {
        btnNo.setOnClickListener {
            listener(ResponseType.NO)
            dialog.dismiss()
        }

        btnYes.setOnClickListener {
            listener(ResponseType.YES)
            dialog.dismiss()
        }

        dialogTitle.text = title
        dialogMessage.text = message
        btnNoTitle.text = cancelTitle
        btnYesTitle.text = okTitle
    }

    private fun setupWindow() {
        this.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        this.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_inset)
    }

    class Builder(val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var cancelTitle: String? = null
        private var okTitle: String? = null
        private var isCancelable: Boolean? = null
        private lateinit var onResponse: (r: ResponseType) -> Unit

        fun setTitle(title: String) = apply { this.title = title }
        fun setMessage(message: String) = apply { this.message = message }
        fun setCancelTitle(cancelTitle: String) = apply { this.cancelTitle = cancelTitle }
        fun setOkTitle(okTitle: String) = apply { this.okTitle = okTitle }
        fun onResponse(listener: (ResponseType) -> Unit) = apply { this.onResponse = listener }
        fun isCancelable(bool: Boolean) = apply { this.isCancelable = bool }
        fun build() = CustomDialog(
            context,
            title,
            message,
            okTitle,
            cancelTitle,
            isCancelable,
            onResponse,
        ).show()
    }
}
