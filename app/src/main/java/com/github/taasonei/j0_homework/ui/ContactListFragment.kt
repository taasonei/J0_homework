package com.github.taasonei.j0_homework.ui

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.taasonei.j0_homework.R
import com.github.taasonei.j0_homework.adapter.ContactAdapter
import com.github.taasonei.j0_homework.databinding.FragmentContactListBinding
import com.github.taasonei.j0_homework.model.ShortContact
import com.github.taasonei.j0_homework.viewmodel.ContactListViewModel
import com.github.taasonei.j0_homework.viewmodel.State
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class ContactListFragment : Fragment() {
    companion object {
        const val CONTACT_ID_TAG = "CONTACT_ID"
        private const val TIMEOUT = 500L
    }

    private val viewModel by viewModels<ContactListViewModel>()
    private var adapter: ContactAdapter? = null
    private var searchView: SearchView? = null
    private var pendingQuery: String? = null

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startObservingContacts() else ContactPermissionDialog().show(
            childFragmentManager,
            ContactPermissionDialog.TAG
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactAdapter()
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        val drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.divider)
        if (drawable != null) dividerItemDecoration.setDrawable(drawable)
        binding.apply {
            contactListRecyclerView.adapter = adapter
            contactListRecyclerView.addItemDecoration(dividerItemDecoration)
            contactListReload.setOnClickListener { startObservingContacts() }
        }
    }

    @OptIn(FlowPreview::class)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.search_bar)
        searchView = searchItem.actionView as SearchView
        val search = searchView
        if (search != null) {
            search.queryHint = getString(R.string.search_hint)
            search.maxWidth = Int.MAX_VALUE

            if (!pendingQuery.isNullOrEmpty()) {
                searchItem.expandActionView()
                search.setQuery(pendingQuery, false)
            }

            search.collectAsStateFlow()
                .debounce(TIMEOUT)
                .onEach { viewModel.loadContacts(it) }
                .launchIn(lifecycleScope)
        }
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView?.setOnQueryTextListener(null)
        searchView = null
    }

    override fun onStart() {
        super.onStart()
        checkPermissionGranted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }

    private fun startObservingContacts() {
        viewModel.loadContacts(null)
        pendingQuery = viewModel.searchViewQuery.value
        viewModel.contacts
            .onEach { state ->
                when (state) {
                    is State.Error -> {
                        hideProgressBar()
                        binding.contactListReload.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is State.Loading -> showProgressBar()
                    is State.Success<List<ShortContact>> -> {
                        val contactAdapter = adapter
                        if (contactAdapter != null) {
                            hideProgressBar()
                            contactAdapter.submitList(state.data)
                        }
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun hideProgressBar() {
        binding.apply {
            contactListProgressBar.visibility = View.GONE
            contactListRecyclerView.visibility = View.VISIBLE
            contactListReload.visibility = View.GONE
        }
    }

    private fun showProgressBar() {
        binding.apply {
            contactListProgressBar.visibility = View.VISIBLE
            contactListRecyclerView.visibility = View.GONE
            contactListReload.visibility = View.GONE
        }
    }

    private fun checkPermissionGranted() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> startObservingContacts()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                READ_CONTACTS
            ) -> ContactPermissionDialog().show(
                childFragmentManager,
                ContactPermissionDialog.TAG
            )
            else -> requestPermissionLauncher.launch(READ_CONTACTS)
        }
    }

    private fun SearchView.collectAsStateFlow(): StateFlow<String> {
        val input = MutableStateFlow(query.toString())

        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    input.value = query
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    input.value = newText
                }
                return true
            }
        })
        return input.asStateFlow()
    }

}
