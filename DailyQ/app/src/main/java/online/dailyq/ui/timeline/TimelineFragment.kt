package online.dailyq.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import online.dailyq.databinding.FragmentTimelineBinding
import online.dailyq.ui.base.BaseFragment

class TimelineFragment : BaseFragment() {
    var _binding: FragmentTimelineBinding? = null
    val binding get() = _binding!!
    lateinit var adapter: TimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            adapter = TimelineAdapter(requireContext())
            recycler.adapter =
                adapter.withLoadStateFooter(TimelineLoadStateAdapter { adapter.retry() })
            recycler.layoutManager = LinearLayoutManager(context)
        }

        lifecycleScope.launch {
            Pager(PagingConfig(initialLoadSize = 6, pageSize = 3, enablePlaceholders = false)) {
                TimelinePagingSource(api)
            }.flow.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
