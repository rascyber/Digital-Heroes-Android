package com.josex2r.digitalheroes.controllers;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.josex2r.digitalheoroes.R;
import com.josex2r.digitalheroes.model.BitmapCollection;
import com.josex2r.digitalheroes.model.Blog;
import com.josex2r.digitalheroes.model.Post;
import com.josex2r.digitalheroes.model.PostViewHolder;

public class PostsAdapter extends ArrayAdapter<Post>{

	private Context context;
	private List<Post> news;
	private int resource;
	private ListView lvPosts;
	private BitmapCollection images;
	private OnClickListener listener;
	
	public PostsAdapter(Context context, int resource, List<Post> objects, ListView lv, OnClickListener li) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.resource=resource;
		this.context=context;
		this.news=objects;
		this.lvPosts=lv;
		this.images=Blog.getInstance().getImages();
		this.listener=li;
	}
	
	public ListView getListView(){
		return lvPosts;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View row=convertView;
		PostViewHolder viewHolder;
		Post currPost=news.get(position);
		
		if(row==null){
			LayoutInflater inflater=((Activity) context).getLayoutInflater();
			row=inflater.inflate(resource, null);
			viewHolder=new PostViewHolder();
			viewHolder.lblTitle=(TextView) row.findViewById(R.id.lblTitle);
			viewHolder.lblDescription=(TextView) row.findViewById(R.id.lblDescription);
			viewHolder.ivImage=(ImageView) row.findViewById(R.id.ivImage);
			viewHolder.pbImage=(ProgressBar) row.findViewById(R.id.pbImage);
			viewHolder.ivFavourites=(ImageView)row.findViewById(R.id.ivFavourites);
			row.setTag(viewHolder);
		}else{
			viewHolder=(PostViewHolder) row.getTag();
		}
		viewHolder.position=position;

		viewHolder.lblTitle.setText( currPost.getTitle() );
		viewHolder.lblDescription.setText( currPost.getDescription() );
		viewHolder.pbImage.setIndeterminate(true);
		hideImage(viewHolder);
		
		
		if(currPost.getImageLink().equals("NO-IMAGE")){

			//Log.d("MyApp","NO-IMAGE");
			//Log.d("MyApp",images.getBitmapFromMemCache("NO-IMAGE").toString());
			viewHolder.ivImage.setImageBitmap( images.getBitmapFromMemCache("NO-IMAGE") );
			showImage(viewHolder);
			
		}else{
			//Log.d("MyApp","Check if image exist");
			
			if(images.getBitmapFromMemCache( currPost.getImageLink() )==null){
				
				//Log.d("MyApp","currPost.getImage()==null");
				ImageLoader downloader=new ImageLoader(position);
				downloader.postHolder=viewHolder;
		
				downloader.execute( currPost.getImageLink() );
				
			}else{
			
				viewHolder.ivImage.setImageBitmap( images.getBitmapFromMemCache(currPost.getImageLink()) );
				showImage(viewHolder);
			
			}
			
		}
		
		
		Blog blog=Blog.getInstance();
		
		if(blog.isFavourite(currPost.getLink())){
			viewHolder.ivFavourites.setImageDrawable(context.getResources().getDrawable(android.R.drawable.star_on));
		}else{
			viewHolder.ivFavourites.setImageDrawable(context.getResources().getDrawable(android.R.drawable.star_off));
		}
		
		viewHolder.ivFavourites.setTag(position);
		viewHolder.ivFavourites.setOnClickListener(this.listener);
		
		return row;
	}
	
	private void hideImage(PostViewHolder viewHolder){
		viewHolder.pbImage.setVisibility(View.VISIBLE);
		viewHolder.ivImage.setVisibility(View.GONE);
	}
	
	private void showImage(PostViewHolder viewHolder){
		viewHolder.pbImage.setVisibility(View.GONE);
		viewHolder.ivImage.setVisibility(View.VISIBLE);
	}
	
	public class ImageLoader extends AsyncTask<String, Integer, Bitmap>{
		
		public PostViewHolder postHolder;
		private int position;
		
		public ImageLoader(int position){
			this.position=position;
		}

		@Override
		protected Bitmap doInBackground(String... url) {
			// TODO Auto-generated method stub
			//Log.d("MyApp","Trying to download image");
			URL postUrl;
			Bitmap bitmap;
			try {
				if(url[0].equals("NO-IMAGE"))
					throw new Exception();
				
				postUrl=new URL(url[0]);
		        URLConnection conn=postUrl.openConnection();
		        conn.connect();
		        InputStream stream=postUrl.openStream();
		        bitmap = BitmapFactory.decodeStream(stream);

		        //post.setImage( bitmap );
		        

		    }catch(Exception e){
		    	bitmap=images.getBitmapFromMemCache("NO-IMAGE");
		    	//post.setImage( bitmap );
		    	return null;
		    }
			images.addBitmapToMemoryCache(url[0], bitmap);
			
			return images.getBitmapFromMemCache(url[0]);
			
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			//Prevents lazy image load
			if(this.position==postHolder.position){
				postHolder.ivImage.setImageBitmap( result );
				showImage(postHolder);
			}

			super.onPostExecute(result);
		}
	}

}
