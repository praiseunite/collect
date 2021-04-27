package org.odk.collect.android.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.projects.Project

class CurrentProjectViewModel(currentProjectProvider: CurrentProjectProvider) : ViewModel() {

    private val _currentProject = MutableLiveData(Consumable(currentProjectProvider.getCurrentProject()!!))
    val currentProject: LiveData<Consumable<Project>> = _currentProject

    fun setCurrentProject(project: Project) {
        _currentProject.postValue(Consumable(project))
    }

    open class Factory constructor(private val currentProjectProvider: CurrentProjectProvider) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CurrentProjectViewModel(currentProjectProvider) as T
        }
    }
}
