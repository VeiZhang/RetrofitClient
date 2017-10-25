package com.excellence.retrofit.sample;

import java.util.List;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/24
 *     desc   :
 * </pre>
 */

public class Gank
{

	/**
	 * error : false
	 * results : [{"_id":"59ee8adf421aa90fe50c019b","createdAt":"2017-10-24T08:35:43.61Z","desc":"10-24","publishedAt":"2017-10-24T11:50:49.1Z","source":"chrome","type":"福利","url":"http://7xi8d6.com1.z0.glb.clouddn.com/20171024083526_Hq4gO6_bluenamchu_24_10_2017_8_34_28_246.jpeg","used":true,"who":"代码家"}]
	 */

	private boolean error;
	private List<ResultsBean> results;

	public boolean isError()
	{
		return error;
	}

	public void setError(boolean error)
	{
		this.error = error;
	}

	public List<ResultsBean> getResults()
	{
		return results;
	}

	public void setResults(List<ResultsBean> results)
	{
		this.results = results;
	}

	public static class ResultsBean
	{
		/**
		 * _id : 59ee8adf421aa90fe50c019b
		 * createdAt : 2017-10-24T08:35:43.61Z
		 * desc : 10-24
		 * publishedAt : 2017-10-24T11:50:49.1Z
		 * source : chrome
		 * type : 福利
		 * url : http://7xi8d6.com1.z0.glb.clouddn.com/20171024083526_Hq4gO6_bluenamchu_24_10_2017_8_34_28_246.jpeg
		 * used : true
		 * who : 代码家
		 */

		private String _id;
		private String createdAt;
		private String desc;
		private String publishedAt;
		private String source;
		private String type;
		private String url;
		private boolean used;
		private String who;

		public String get_id()
		{
			return _id;
		}

		public void set_id(String _id)
		{
			this._id = _id;
		}

		public String getCreatedAt()
		{
			return createdAt;
		}

		public void setCreatedAt(String createdAt)
		{
			this.createdAt = createdAt;
		}

		public String getDesc()
		{
			return desc;
		}

		public void setDesc(String desc)
		{
			this.desc = desc;
		}

		public String getPublishedAt()
		{
			return publishedAt;
		}

		public void setPublishedAt(String publishedAt)
		{
			this.publishedAt = publishedAt;
		}

		public String getSource()
		{
			return source;
		}

		public void setSource(String source)
		{
			this.source = source;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public boolean isUsed()
		{
			return used;
		}

		public void setUsed(boolean used)
		{
			this.used = used;
		}

		public String getWho()
		{
			return who;
		}

		public void setWho(String who)
		{
			this.who = who;
		}
	}
}
