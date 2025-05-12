package com.example.movielab4.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielab4.R
import com.example.movielab4.api.RetrofitClient
import com.example.movielab4.databinding.FragmentSearchBinding
import com.example.movielab4.model.MovieResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MovieAdapter(emptyList()) { movie ->
            val bundle = Bundle().apply {
                putSerializable("movie", movie)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MovieDetailFragment::class.java, bundle)
                .addToBackStack(null)
                .commit()
        }
        binding.rvMovies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMovies.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val query = binding.etQuery.text.toString().trim()
            if (query.isNotEmpty()) searchMovies(query)
        }
    }

    private fun searchMovies(query: String) {
        RetrofitClient.apiService
            .searchMovies(
                query = query,
                apiKey = "3d42ea9b7257017ca8fa75d3cde57a1c"
            )
            .enqueue(object : Callback<MovieResponse> {
                override fun onResponse(
                    call: Call<MovieResponse>,
                    response: Response<MovieResponse>
                ) {
                    if (response.isSuccessful) {
                        val movies = response.body()?.results ?: emptyList()
                        adapter.updateData(movies)
                    } else {
                        Toast.makeText(requireContext(),
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    Toast.makeText(requireContext(),
                        "Failed: ${t.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}