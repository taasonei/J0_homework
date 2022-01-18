package com.github.taasonei.j0_homework.ui

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.taasonei.j0_homework.R
import com.github.taasonei.j0_homework.adapter.ContactAdapter
import com.github.taasonei.j0_homework.databinding.FragmentContactListBinding
import com.github.taasonei.j0_homework.viewmodel.ContactListViewModel

class ContactListFragment : Fragment() {
    companion object {
        const val CONTACT_ID_TAG = "CONTACT_ID"
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
        }
    }

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

            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.searchContacts(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.searchContacts(newText)
                    return true
                }
            })
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
        pendingQuery = viewModel.searchViewQuery.value
        viewModel.contacts.observe(viewLifecycleOwner, { list ->
            val contactAdapter = adapter
            if (contactAdapter != null) {
                hideProgressBar()
                contactAdapter.submitList(list)
            }
        })
    }

    private fun hideProgressBar() {
        binding.apply {
            contactListProgressBar.visibility = View.GONE
            contactListRecyclerView.visibility = View.VISIBLE
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

}
