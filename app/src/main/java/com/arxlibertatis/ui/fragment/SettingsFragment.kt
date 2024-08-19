package com.arxlibertatis.ui.fragment

import android.app.Activity
import android.content.Intent
import android.content.Intent.createChooser
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.arxlibertatis.R
import com.arxlibertatis.interfaces.SettingsFragmentMvpView
import com.arxlibertatis.presenter.SettingsFragmentPresenter
import com.arxlibertatis.utils.GAME_FILES_SHARED_PREFS_KEY
import com.arxlibertatis.utils.extensions.changeInputTypeToDecimal
import com.arxlibertatis.utils.extensions.setHint
import moxy.presenter.InjectPresenter


class SettingsFragment : MvpAppCompatFragment(), SettingsFragmentMvpView,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val CHOOSE_DIRECTORY_REQUEST_CODE = 4321

    @InjectPresenter
    lateinit var presenter: SettingsFragmentPresenter

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.settings)

        val gameFilesPreference = findPreference<Preference>(GAME_FILES_SHARED_PREFS_KEY)
        gameFilesPreference?.setOnPreferenceClickListener {
            with(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)) {
                addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(createChooser(this, "Choose directory"),
                    CHOOSE_DIRECTORY_REQUEST_CODE)
            }
            true
        }
        updatePreference(gameFilesPreference!!,GAME_FILES_SHARED_PREFS_KEY)

        findPreference<Preference>("screen_controls_settings")?.setOnPreferenceClickListener {
            presenter.onConfigureScreenControlsClicked(requireContext())
            true
        }

        val customResolution = findPreference<EditTextPreference>("custom_resolution")
        customResolution?.setHint(R.string.custom_resolution_hint)

        val hudScale = findPreference<EditTextPreference>("hud_scale")
        hudScale?.changeInputTypeToDecimal()

        val cursorScale = findPreference<EditTextPreference>("cursor_scale")
        cursorScale?.changeInputTypeToDecimal()

        val fontSize = findPreference<EditTextPreference>("font_size")
        fontSize?.changeInputTypeToDecimal()

        updatePreference(customResolution!!, "custom_resolution")
        updatePreference(hudScale!!, "hud_scale")
        updatePreference(cursorScale!!,"cursor_scale")
        updatePreference(fontSize!!,"font_size")

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.copy_game_assets -> {
                presenter.copyGameAssets(requireContext(), preferenceScreen.sharedPreferences!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when{
            resultCode != Activity.RESULT_OK -> return
            requestCode == CHOOSE_DIRECTORY_REQUEST_CODE ->
            {
                presenter.saveGamePath(data!!,requireContext(),this.preferenceScreen.sharedPreferences!!)
            }
        }
    }

    override fun updatePreference (prefsKey : String) =
        updatePreference(findPreference(prefsKey)!!,prefsKey)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key!="command_line") {
            updatePreference(key!!)
        }
    }

    private fun updatePreference (preference: Preference, prefsKey: String){
        preference.summary = preferenceScreen.sharedPreferences?.getString(prefsKey, "") ?: ""
    }
}