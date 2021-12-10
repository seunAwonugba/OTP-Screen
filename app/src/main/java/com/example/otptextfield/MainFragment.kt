package com.example.otptextfield

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.otptextfield.constant.Constants.TEST_VERIFY_CODE
import com.example.otptextfield.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class MainFragment : Fragment(R.layout.fragment_main) {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        setListener()

        initiateFocus()
    }

    private fun setListener() {
        binding.otpLayoutId.setOnClickListener {
            val inputManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(binding.otpLayoutId.windowToken, 0)
        }

        setTextChangeListener(fromEditText = binding.first, targetEditText = binding.second)
        setTextChangeListener(fromEditText = binding.second, targetEditText = binding.third)
        setTextChangeListener(fromEditText = binding.third, targetEditText = binding.fourth)
        setTextChangeListener(fromEditText = binding.fourth, done = {verifyOTPCode()})

        setKeyListener(fromEditText = binding.second, backToEditText = binding.first)
        setKeyListener(fromEditText = binding.third, backToEditText = binding.second)
        setKeyListener(fromEditText = binding.fourth, backToEditText = binding.third)
    }

    //upon fragment launch, launch keyboard, set focus on first edit text field
    private fun initiateFocus(){
        binding.first.isEnabled = true

        binding.first.postDelayed({
            binding.first.requestFocus()
            
            val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(binding.first, InputMethodManager.SHOW_FORCED)
        }, 1000)
    }

    //create focus reset method
    private fun resetFocus(){
        binding.first.isEnabled = false
        binding.second.isEnabled = false
        binding.third.isEnabled = false
        binding.fourth.isEnabled = false

        binding.first.setText("")
        binding.second.setText("")
        binding.third.setText("")
        binding.fourth.setText("")

        initiateFocus()
    }

    //create text change listener method for all edit text fields
    private fun setTextChangeListener(
        fromEditText : TextInputEditText,
        targetEditText : TextInputEditText? = null,
        done: (() -> Unit)? = null
    ){
        fromEditText.addTextChangedListener {
            it?.let { string->
                if (string.isNotEmpty()){

                    targetEditText?.let { textInputEditText ->
                        textInputEditText.isEnabled = true
                        textInputEditText.requestFocus()
                    } ?: run {
                        done ?.let { done ->
                            done()
                        }
                    }

                    fromEditText.clearFocus()

                    //the very moment you input OTP in any text field, it jumps to the next input field
                    fromEditText.isEnabled = false
                }
            }
        }

    }

    //create key listener method for edit texts
    private fun setKeyListener(fromEditText: TextInputEditText, backToEditText: TextInputEditText){
        fromEditText.setOnKeyListener { _, _, event ->

            if (null != event && KeyEvent.KEYCODE_DEL == event.keyCode){
                backToEditText.isEnabled = true
                backToEditText.requestFocus()
                backToEditText.setText("")

                fromEditText.clearFocus()
                fromEditText.isEnabled = false
            }

            false
        }

    }

    //create OTP verification method
    private fun verifyOTPCode(){
        val otpCode = binding.first.text.toString().trim() +
                binding.second.text.toString().trim() +
                binding.third.text.toString().trim() +
                binding.fourth.text.toString().trim()

        if (4 != otpCode.length){
            return
        }

        if (otpCode == TEST_VERIFY_CODE){

            Snackbar.make(binding.root, "OTP verified", Snackbar.LENGTH_LONG).show()
            val inputManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(binding.otpLayoutId.windowToken, 0)

            return
        }
        Snackbar.make(binding.root, "Invalid OTP, input a valid OTP", Snackbar.LENGTH_LONG).show()
        resetFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}