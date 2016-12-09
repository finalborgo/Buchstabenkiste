package com.books.leemon.buchstabenkiste.apis;

import com.books.leemon.buchstabenkiste.models.pojo.GoogleBooksModel;


import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksApi {

    String GOOGLE_BOOKS_API_ENDPOINT = "https://www.googleapis.com/books/";
    String GOOGLE_BOOKS_API_KEY = "AIzaSyD_oJEiHuhLj5vM6bD9D4_2YBFyuYigCFA";

    //Call for search with isbn
    @GET("v1/volumes/?key=" + GOOGLE_BOOKS_API_KEY)
    Call<GoogleBooksModel> getBookDataForISBN(@Query("q") String isbn );


    //Call for search with author and title --> intitle=? and inauthor=?
    // example: https://www.googleapis.com/books/v1/volumes?q=inauthor:joanne+rowling+intitle:harry+potter+und+die+kammer+des+schreckens
    @GET("v1/volumes/?key=" + GOOGLE_BOOKS_API_KEY)
    Call<GoogleBooksModel> getBookDataForAuthorAndTitle(@Query("q") String authorTitle);

    class Factory {
        private static GoogleBooksApi service;

        public static GoogleBooksApi getInstance() {
            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(GOOGLE_BOOKS_API_ENDPOINT)
                        .build();
                service = retrofit.create(GoogleBooksApi.class);

            }
            return service;
        }
    }

}
