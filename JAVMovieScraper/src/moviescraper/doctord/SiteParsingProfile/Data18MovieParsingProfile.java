package moviescraper.doctord.SiteParsingProfile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import moviescraper.doctord.dataitem.Actor;
import moviescraper.doctord.dataitem.Director;
import moviescraper.doctord.dataitem.Genre;
import moviescraper.doctord.dataitem.ID;
import moviescraper.doctord.dataitem.MPAARating;
import moviescraper.doctord.dataitem.OriginalTitle;
import moviescraper.doctord.dataitem.Outline;
import moviescraper.doctord.dataitem.Plot;
import moviescraper.doctord.dataitem.Rating;
import moviescraper.doctord.dataitem.ReleaseDate;
import moviescraper.doctord.dataitem.Runtime;
import moviescraper.doctord.dataitem.Set;
import moviescraper.doctord.dataitem.SortTitle;
import moviescraper.doctord.dataitem.Studio;
import moviescraper.doctord.dataitem.Tagline;
import moviescraper.doctord.dataitem.Thumb;
import moviescraper.doctord.dataitem.Title;
import moviescraper.doctord.dataitem.Top250;
import moviescraper.doctord.dataitem.Votes;
import moviescraper.doctord.dataitem.Year;
import moviescraper.doctord.model.Movie;
import moviescraper.doctord.model.SearchResult;

public class Data18MovieParsingProfile extends SiteParsingProfile {
	
	boolean useSiteSearch = true;
	String yearFromFilename = "";
	String fileName;
	//I've unfortunately had to make this static due to the current mess of a way this type of scraping is done where the object used
	//to create the search results is not the same as the object used to actually scrape the document.
	private static HashMap<String, String> releaseDateMap; 


	@Override
	public Title scrapeTitle() {
		Element titleElement = document.select("div#centered.main2 div h1").first();
		if(titleElement != null)
			return new Title(titleElement.text());
		else return new Title("");
	}

	@Override
	public OriginalTitle scrapeOriginalTitle() {
		return OriginalTitle.BLANK_ORIGINALTITLE;
	}

	@Override
	public SortTitle scrapeSortTitle() {
		// TODO Auto-generated method stub
		return SortTitle.BLANK_SORTTITLE;
	}

	@Override
	public Set scrapeSet() {
		Element setElement = document.select("div div.p8 div p a[href*=/series/]").first();
		if(setElement != null)
			return new Set(setElement.text());
		else return Set.BLANK_SET;
	}

	@Override
	public Rating scrapeRating() {
		// TODO Auto-generated method stub
		return Rating.BLANK_RATING;
	}

	@Override
	public Year scrapeYear() {

		//old method before site update in September 2014
		Element releaseDateElement = document.select("div p:contains(Release Date:) b").first();
		//new method after site update in mar 2015
		if(releaseDateElement == null)
		{

			releaseDateElement = document.select("div.p8 div.gen12 p:contains(Release Date:), div.p8 div.gen12 p:contains(Production Year:)").first();
			if(releaseDateElement != null)
			{
				String releaseDateText = releaseDateElement.text().trim();
				final Pattern pattern = Pattern.compile("(\\d{4})"); //4 digit years
				final Matcher matcher = pattern.matcher(releaseDateText);
				if ( matcher.find() ) {
					String year = matcher.group(matcher.groupCount());
					return new Year(year);
				}
				if(releaseDateText.length() > 4)
				{
					//just get the first 4 letters which is the year
					releaseDateText = releaseDateText.substring(0,4);
					return new Year(releaseDateText);
				}
				else return Year.BLANK_YEAR;
			}
		}
		else if(releaseDateElement != null)
		{
			String releaseDateText = releaseDateElement.text().trim();
			//just get the last 4 letters which is the year
			if(releaseDateText.length() >= 4)
			{
				releaseDateText = releaseDateText.substring(releaseDateText.length()-4,releaseDateText.length());
				return new Year(releaseDateText);
			}
		}
		return Year.BLANK_YEAR;
	}

	@Override
	public Top250 scrapeTop250() {
		// TODO Auto-generated method stub
		return Top250.BLANK_TOP250;
	}

	@Override
	public Votes scrapeVotes() {
		// TODO Auto-generated method stub
		return Votes.BLANK_VOTES;
	}

	@Override
	public Outline scrapeOutline() {
		// TODO Auto-generated method stub
		return Outline.BLANK_OUTLINE;
	}

	@Override
	public Plot scrapePlot() {
		Element plotElement = document.select("p.gen12:contains(Description:)").first();
		if(plotElement != null)
		{
			String plotText = plotElement.text();
			if(plotText.startsWith("Description: "))
			{
				plotText = plotText.replaceFirst("Description:", "");
			}
			return new Plot(plotText);
		}
		return Plot.BLANK_PLOT;
	}

	@Override
	public Tagline scrapeTagline() {
		return Tagline.BLANK_TAGLINE;
	}

	@Override
	public Runtime scrapeRuntime() {
		Element runtimeElement = document.select("p.gen12:contains(Length:)").first();
		if(runtimeElement != null)
		{
			String runtimeElementText = runtimeElement.text().replaceFirst(Pattern.quote("Length:"), "").replaceFirst(Pattern.quote(" min."), "").trim();
			return new Runtime(runtimeElementText);
		}
		else return Runtime.BLANK_RUNTIME;
	}

	@Override
	public Thumb[] scrapePosters() {
		Element posterElement = document.select("a[rel=covers]").first();
		if(posterElement != null)
		{
			Thumb[] posterThumbs = new Thumb[1];
			try {
				posterThumbs[0] = new Thumb(posterElement.attr("href"));
				return posterThumbs;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return new Thumb[0];
			}
		}
		return new Thumb[0];
	}

	@Override
	public Thumb[] scrapeFanart() {
		Element posterElement = document.select("a[rel=covers]:contains(Back Cover)").first();
		if(posterElement != null)
		{
			Thumb[] posterThumbs = new Thumb[1];
			try {
				posterThumbs[0] = new Thumb(posterElement.attr("href"));
				return posterThumbs;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return new Thumb[0];
			}
		}
		return new Thumb[0];
	}

	@Override
	public Thumb[] scrapeExtraFanart() {
		//find split scene links from a full movie
		Elements sceneContentLinks = document.select("div[onmouseout]:matches(Scene \\d\\d?)");
		ArrayList<String> contentLinks = new ArrayList<String>();
		ArrayList<Thumb> extraFanart = new ArrayList<Thumb>();
		if(sceneContentLinks != null)
		{
			//get just the id from url of the content
			for(Element sceneContentLink : sceneContentLinks)
			{
				Element linkElement = sceneContentLink.select("a[href*=/content/").first();
				if(linkElement != null)
					{
					String linkElementURL = linkElement.attr("href");
					if(linkElementURL.contains("/"))
					{
						String contentID = linkElementURL.substring(linkElementURL.lastIndexOf("/")+1,linkElementURL.length());
						contentLinks.add(contentID);
					}
				}
			}
		}
		
		//for each id, go to the viewer page for that ID
		for(String contentID : contentLinks)
		{
			//int viewerPageNumber = 1;
			for(int viewerPageNumber = 1; viewerPageNumber <= 15; viewerPageNumber++)
			{
				String currentViewerPageURL = "http://www.data18.com/viewer/" + contentID + "/" + String.format("%02d", viewerPageNumber);
				try {
					
					Document viewerDocument = Jsoup.connect(currentViewerPageURL).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0").get();
					if(viewerDocument!= null)
					{
						Element imgElement = viewerDocument.select("div#post_view a[href*=/viewer/] img").first();
						if(imgElement != null)
						{
							String mainImageUrl = imgElement.attr("src");
							Thumb thumbToAdd = new Thumb(mainImageUrl);
							String previewURL = mainImageUrl.substring(0,mainImageUrl.length()-6) + "th8/" + mainImageUrl.substring(mainImageUrl.length()-6,mainImageUrl.length());
							if(fileExistsAtURL(previewURL))
								thumbToAdd.setPreviewURL(new URL(previewURL));
							extraFanart.add(thumbToAdd);
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					//continue;
			}
			
			}
		}
		return extraFanart.toArray(new Thumb[extraFanart.size()]);
	}

	@Override
	public MPAARating scrapeMPAA() {
		return MPAARating.RATING_XXX;
	}

	@Override
	public ID scrapeID() {
		return ID.BLANK_ID;
	}

	@Override
	public ArrayList<Genre> scrapeGenres() {
		ArrayList<Genre> genreList = new ArrayList<Genre>();
		Elements genreElements = document.select("div.gen12:contains(Categories:) a, div.p8 div:contains(Categories:) a");
		if (genreElements != null)
		{
			for(Element currentGenreElement : genreElements)
			{
				String genreText = currentGenreElement.text().trim();
				if(genreText != null && genreText.length() > 0)
					genreList.add(new Genre(genreText));
			}
		}
		
		return genreList;
	}

	@Override
	public ArrayList<Actor> scrapeActors() {
		Elements actorElements = document.select("p.line1 a img");
		ArrayList<Actor> actorList = new ArrayList<Actor>();
		if(actorElements != null)
		{
			for(Element currentActorElement : actorElements)
			{
				String actorName = currentActorElement.attr("alt");
				String actorThumbnail = currentActorElement.attr("src");
				
				//case with actor with thumbnail
				if(actorThumbnail != null && !actorThumbnail.equals("http://img.data18.com/images/no_prev_60.gif"))
				{
					try {
						actorThumbnail = actorThumbnail.replaceFirst(Pattern.quote("/60/"), "/120/");
						actorList.add(new Actor(actorName, null, new Thumb(actorThumbnail)));
					} catch (MalformedURLException e) {
						actorList.add(new Actor(actorName, null, null));
						e.printStackTrace();
					}
				}
				//add the actor with no thumbnail
				else
				{
					actorList.add(new Actor(actorName, null, null));
				}
			}
		}
		
		Elements otherActors = document.select("[href^=http://www.data18.com/dev/]");
		if(otherActors != null) {
			for (Element element : otherActors) {
				String actorName = element.attr("alt");
				actorName = element.childNode(0).toString();
				actorList.add(new Actor(actorName, null, null));
			}
		}
		
		return actorList;
	}

	@Override
	public ArrayList<Director> scrapeDirectors() {
		ArrayList<Director> directorList = new ArrayList<Director>();
		Element directorElement = document.select("a[href*=director=]").first();
		if(directorElement != null)
		{
			String directorName = directorElement.text();
			if(directorName != null && directorName.length() > 0 && !directorName.equals("Unknown"))
				directorList.add(new Director(directorName,null));
		}
		return directorList;
	}

	@Override
	public Studio scrapeStudio() {
		Element studioElement = document.select("div div.p8 div p a[href*=/studios/").first();
		if(studioElement != null)
		{
			String studioText = studioElement.text().trim();
			if(studioText != null && studioText.length() > 0)
				return new Studio(studioText);
		}
		return Studio.BLANK_STUDIO;
	}

	@Override
	public String createSearchString(File file) {
		scrapedMovieFile = file;
		String fileBaseName;
		if(file.isFile())
			fileBaseName = FilenameUtils.getBaseName(Movie.getUnstackedMovieName(file));
		else
			fileBaseName = file.getName();
		fileName = fileBaseName;
		String [] splitBySpace = fileBaseName.split(" ");
		if(splitBySpace.length > 1)
		{
			//check if last word in filename contains a year like (2012) or [2012]
			if(splitBySpace[splitBySpace.length-1].matches("[\\(\\[]\\d{4}[\\)\\]]"))
			{
				yearFromFilename = splitBySpace[splitBySpace.length-1].replaceAll("[\\(\\[\\)\\]]", "");
				fileBaseName = fileBaseName.replaceFirst("[\\(\\[]\\d{4}[\\)\\]]","").trim();

			}
		}
		if(useSiteSearch)
		{
			URLCodec codec = new URLCodec();
			try {
				fileBaseName = codec.encode(fileBaseName);
			} catch (EncoderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileBaseName = "http://www.data18.com/search/?k=" + fileBaseName + "&t=2";
			return fileBaseName;
		}
		return FilenameUtils.getBaseName(file.getName());
	}

	@Override
	public SearchResult[] getSearchResults(String searchString)
			throws IOException {
		System.out.println("Trying to scrape with URL = " + searchString);
		if(useSiteSearch)
		{
			ArrayList<SearchResult> linksList = new ArrayList<SearchResult>();
			Document doc = Jsoup.connect(searchString).userAgent("Mozilla").ignoreHttpErrors(true).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();
			Elements movieSearchResultElements = doc.select("div[style=float: left; padding: 6px; width: 130px;");
			if(movieSearchResultElements == null || movieSearchResultElements.size() == 0)
			{
				this.useSiteSearch = false;
				return getLinksFromGoogle(fileName.replace("-", ""), "data18.com/movies/");
			}
			else
			{
				for(Element currentMovie : movieSearchResultElements)
				{
					String currentMovieURL = currentMovie.select("a").first().attr("href");
					String currentMovieTitle = currentMovie.select("a").last().text();
					String releaseDateText = currentMovie.ownText();
					if(releaseDateText != null && releaseDateText.length() > 0)
						currentMovieTitle = currentMovieTitle + " (" + releaseDateText + ")";
					Thumb currentMovieThumb = new Thumb(currentMovie.select("img").attr("src"));
					linksList.add(new SearchResult(currentMovieURL, currentMovieTitle, currentMovieThumb));
					if(releaseDateMap == null)
						releaseDateMap = new HashMap<String, String>();
					//I'm putting into a static variable that never gets freed, so this could be a potential memory leak
					//TODO: find a better way to do this without a global variable
					releaseDateMap.put(currentMovieURL, releaseDateText);
				}
				return linksList.toArray(new SearchResult[linksList.size()]);
			}
		}
		else
		{
			this.useSiteSearch = false;
			return getLinksFromGoogle(searchString, "data18.com/movies/");
		}
	}
	@Override
	public String toString(){
		return "Data18Movie";
	}

	@Override
	public SiteParsingProfile newInstance() {
		return new Data18MovieParsingProfile();
	}

	@Override
	public String getParserName() {
		return "Data18 Movie";
	}

	@Override
	public ReleaseDate scrapeReleaseDate() {
		//Unfortunately this data is not available on full on the page we are scraping, so we store the info from the search result
		//creation and retrieve it here
		if(releaseDateMap != null && releaseDateMap.containsKey(document.location()))
		{
			String releaseDate = releaseDateMap.get(document.location());
			if(releaseDate != null && releaseDate.length() > 4)
				return new ReleaseDate(releaseDate);
		}
		return ReleaseDate.BLANK_RELEASEDATE;
	}

}
