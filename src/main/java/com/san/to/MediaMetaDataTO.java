package com.san.to;

import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaMetaDataTO {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Disposition {

		@JsonProperty("default")
		public int mydefault;

		public int dub;
		public int original;
		public int comment;
		public int lyrics;
		public int karaoke;
		public int forced;
		public int hearing_impaired;
		public int visual_impaired;
		public int clean_effects;
		public int attached_pic;
		public int timed_thumbnails;

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Format {

		public String filename;
		public int nb_streams;
		public int nb_programs;
		public String format_name;
		public String format_long_name;
		public String start_time;
		public String duration;
		public String size;
		public String bit_rate;
		public int probe_score;
		public Tags tags;

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Stream {

		public int index;
		public String codec_name;
		public String codec_long_name;
		public String profile;
		public String codec_type;
		public String codec_time_base;
		public String codec_tag_string;
		public String codec_tag;
		public int width;
		public int height;
		public int coded_width;
		public int coded_height;
		public int has_b_frames;
		public String sample_aspect_ratio;
		public String display_aspect_ratio;
		public String pix_fmt;
		public int level;
		public String color_range;
		public String color_space;
		public String color_transfer;
		public String color_primaries;
		public String chroma_location;
		public int refs;
		public String is_avc;
		public String nal_length_size;
		public String r_frame_rate;
		public String avg_frame_rate;
		public String time_base;
		public int start_pts;
		public String start_time;
		public int duration_ts;
		public String duration;
		public String bit_rate;
		public String bits_per_raw_sample;
		public String nb_frames;
		public Disposition disposition;
		public Tags tags;
		public String sample_fmt;
		public String sample_rate;
		public int channels;
		public String channel_layout;
		public int bits_per_sample;
		public String max_bit_rate;

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Tags {

		public Date creation_time;
		public String language;
		public String handler_name;
		public String encoder;
		public String major_brand;
		public String minor_version;
		public String compatible_brands;

	}

	public ArrayList<Stream> streams;
	public Format format;

}
