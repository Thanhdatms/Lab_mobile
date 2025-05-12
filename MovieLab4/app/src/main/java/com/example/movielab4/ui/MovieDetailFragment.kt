package com.example.movielab4.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.movielab4.R
import com.example.movielab4.databinding.FragmentMovieDetailBinding
import com.example.movielab4.model.Movie

class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {
    private val movie: Movie by lazy {
        requireArguments().getSerializable("movie") as Movie
    }

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentMovieDetailBinding.bind(view)

        // Populate UI
        binding.tvDetailTitle.text    = movie.title
        binding.tvDetailSubtitle.text = movie.originalTitle
        binding.tvDetailOverview.text = movie.overview

        movie.backdropPath?.let {
            val url = "https://image.tmdb.org/t/p/w780$it"
            Glide.with(binding.ivBackdrop)
                .load(url)
                .into(binding.ivBackdrop)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}