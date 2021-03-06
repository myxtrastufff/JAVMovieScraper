package moviescraper.doctord.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import moviescraper.doctord.SiteParsingProfile.SiteParsingProfile;
import moviescraper.doctord.dataitem.Actor;
import moviescraper.doctord.dataitem.Genre;
import moviescraper.doctord.preferences.MoviescraperPreferences;

public class Renamer {

	private String renameString;
	private Movie movie;
	private String sanitizer;
	private File oldFile;
	
	private String extension;
	private String filename;
	private String path;
	private static final int maxFileNameLength = 250;

	private final static String ID = "<ID>";
	private final static String TITLE = "<TITLE>";
	private final static String ACTORS = "<ACTORS>";
	private final static String YEAR = "<YEAR>";
	private final static String ORIGINALTITLE = "<ORIGINALTITLE>";
	private final static String SET = "<SET>";
	private final static String STUDIO = "<STUDIO>";
	private final static String GENRES = "<GENRES>";
	private final static String[] availableRenameTags = {ID, TITLE, ACTORS, GENRES, SET, STUDIO, YEAR, ORIGINALTITLE};
	
	public Renamer(String renameString, String sanitizer, Movie toRename, File oldFile) {
		this.renameString = renameString;
		this.sanitizer = sanitizer;
		this.movie = toRename;
		this.oldFile = oldFile;
	}
	
	public String getNewFileName() {
		
		extension = FilenameUtils.getExtension(oldFile.toString());
		if(oldFile.isDirectory())
			extension = "";
		filename = FilenameUtils.getBaseName(oldFile.toString());
		path = FilenameUtils.getFullPath(oldFile.toString());
		String dot = ".";
		if(oldFile.isDirectory())
			dot = "";
		String newName = getSanitizedString (replace());
		newName = path + newName + getAppendix() + getPosterFanartTrailerEnder() + dot + extension;
		
		return newName;
	}
	
	private String replace() {
		String movieID = movie.getId().getId();
		String movieTitle = movie.getTitle().getTitle();
		List<Actor> movieActorsList = movie.getActors();
		String movieActors = combineActorList(movieActorsList);
		String movieYear = movie.getYear().getYear();
		String movieOriginalTitle = movie.getOriginalTitle().getOriginalTitle();
		String movieSet = movie.getSet().getSet();
		String movieStudio = movie.getStudio().getStudio();
		String movieGenres = combineGenreList(movie.getGenres());
		String newName = renameString;
				
		newName = renameReplaceAll(newName, ID, movieID);
		newName = renameReplaceAll(newName, TITLE, movieTitle);
		newName = renameReplaceAll(newName, YEAR, movieYear);
		newName = renameReplaceAll(newName, ORIGINALTITLE, movieOriginalTitle);
		newName = renameReplaceAll(newName, SET, movieSet);
		newName = renameReplaceAll(newName, STUDIO, movieStudio);
		newName = renameReplaceAll(newName, GENRES, movieGenres);
		
		//we need to watch out when renaming a file that a large number of actors doesn't create
		//a movie name that is too long
		String potentialNameWithActors =  renameReplaceAll(newName, ACTORS, movieActors);
		if(potentialNameWithActors.length() < maxFileNameLength )
			newName = potentialNameWithActors;
		else
			newName = renameReplaceAll(newName, ACTORS, "");

		return newName.trim();
	}
	
	private String renameReplaceAll(String replacementString, String tagName, String movieContentOfTag)
	{
		String replacedString = replacementString;
		if(replacedString.contains(tagName)){
			replacedString = StringUtils.replace(replacedString, tagName, movieContentOfTag);
		}
		if(movieContentOfTag == null || movieContentOfTag.trim().equals(""))
		{
			replacedString = replacedString.replaceAll("\\[\\]|\\(\\)", "");
		}
		return replacedString;
	}
	
	private String combineActorList(List<Actor> actors) {
		String actorsString = "";
		for (int i = 0; i < movie.getActors().size(); i++) {
			actorsString += movie.getActors().get(i).getName();
			if (i + 1 < movie.getActors().size())
				actorsString += ", ";
		}
		return actorsString;
	}
	
	public static void rename(File fileToRename, MoviescraperPreferences preferences) throws IOException
	{
		File nfoFile = new File(Movie.getFileNameOfNfo(fileToRename, preferences.getNfoNamedMovieDotNfo()));
		File posterFile = new File(Movie.getFileNameOfPoster(fileToRename, preferences.getNoMovieNameInImageFiles()));
		File fanartFile = new File(Movie.getFileNameOfFanart(fileToRename, preferences.getNoMovieNameInImageFiles()));
		File trailerFile = new File(Movie.getFileNameOfTrailer(fileToRename));
		if(nfoFile != null && nfoFile.exists() && fileToRename.exists())
		{
			Movie movieReadFromNfo = Movie.createMovieFromNfo(nfoFile);
			if(movieReadFromNfo != null && movieReadFromNfo.getTitle() != null)
			{
				Renamer renamer = new Renamer(MoviescraperPreferences.getRenamerString(), MoviescraperPreferences.getSanitizerForFilename(), movieReadFromNfo, fileToRename);
				
				//Figure out all the new names
			    File newMovieFilename = new File(renamer.getNewFileName());
			    
			    renamer.setOldFilename(nfoFile);
			    File newNfoFilename = new File(renamer.getNewFileName());
				
				renamer.setOldFilename(posterFile);
				File newPosterFilename = new File(renamer.getNewFileName());
				
				renamer.setOldFilename(fanartFile);
				File newFanartFilename = new File(renamer.getNewFileName());
				
				renamer.setOldFilename(trailerFile);
				File newTrailerFilename = new File(renamer.getNewFileName());
				
				//Do All the Renames
				if(fileToRename.exists())
				{
					System.out.println("Renaming " + fileToRename.getPath() + " to " + newMovieFilename);
					fileToRename.renameTo(newMovieFilename);
				}
				
				if(nfoFile.exists())
				{
					System.out.println("Renaming " + nfoFile.getPath() + " to " + newNfoFilename);
					FileUtils.moveFile(nfoFile, newNfoFilename);
				}
				
				if(posterFile.exists())
				{
					System.out.println("Renaming " + posterFile.getPath() + " to " + newPosterFilename);
					FileUtils.moveFile(posterFile, newPosterFilename);
				}
				
				if(fanartFile.exists())
				{
					System.out.println("Renaming " + fanartFile.getPath() + " to " + newFanartFilename);
					FileUtils.moveFile(fanartFile, newFanartFilename);
				}
				
				if(trailerFile.exists())
				{
					System.out.println("Renaming " + trailerFile.getPath() + " to " + newTrailerFilename);
					FileUtils.moveFile(trailerFile, newTrailerFilename);
				}
				
				//In case of stacked movie files (Movies which are split into multiple files such AS CD1, CD2, etc) get the list of all files
				//which are part of this movie's stack
				File currentDirectory = fileToRename.getParentFile();
				String currentlySelectedMovieFileWihoutStackSuffix = SiteParsingProfile.stripDiscNumber(FilenameUtils.removeExtension(fileToRename.getName()));
				if(currentDirectory != null)
				{

					for(File currentFile : currentDirectory.listFiles())
					{
						String currentFileNameWithoutStackSuffix = SiteParsingProfile.stripDiscNumber(FilenameUtils.removeExtension(currentFile.getName()));
						if(currentFile.isFile() && currentFileNameWithoutStackSuffix.equals(currentlySelectedMovieFileWihoutStackSuffix))
						{
							renamer.setOldFilename(currentFile);
							File newStackedFilename = new File(renamer.getNewFileName());
							System.out.println("Renaming " + currentFile.getPath() + " to " + newStackedFilename);
							FileUtils.moveFile(currentFile, newStackedFilename);
						}
					}
				}
			}
		}
		else if(!nfoFile.exists())
		{
			System.err.println("No scraped nfo file found for: " + fileToRename + "  - skipping rename.");
		}
	}
	
	private String combineGenreList(List<Genre> genres) {
		String genresString = "";
		for (int i = 0; i < movie.getGenres().size(); i++) {
			genresString += movie.getGenres().get(i).getGenre();
			if (i + 1 < movie.getGenres().size())
				genresString += ", ";
		}
		return genresString;
	}
	
	private String getAppendix() {
		//TODO: make this method more flexible to check all the possible types of disc names
		//(I already have a method somewhere else in this project which has a good regular expression to use)
		String appendix = "";
		boolean hasAppendix = filename.matches(".*CD\\s?1.*");
		if (hasAppendix)
			appendix = " CD1";
		hasAppendix = filename.matches(".*CD\\s?2.*");
		if (hasAppendix)
			appendix = " CD2";
		hasAppendix = filename.matches(".*CD\\s?3.*");
		if (hasAppendix)
			appendix = " CD3";
		hasAppendix = filename.matches(".*CD\\s?4.*");
		if (hasAppendix)
			appendix = " CD4";
		hasAppendix = filename.matches(".*CD\\s?5.*");
		if (hasAppendix)
			appendix = " CD5";
		return appendix;
	}
	
	private String getPosterFanartTrailerEnder(){
		String fileNameEnder = "";
		boolean hasFileNameEnder = oldFile.getPath().matches(".*-poster[\\.].+");
		if (hasFileNameEnder)
			fileNameEnder = "-poster";
		hasFileNameEnder = oldFile.getPath().matches(".*-trailer[\\.].+");
		if (hasFileNameEnder)
			fileNameEnder = "-trailer";
		hasFileNameEnder = oldFile.getPath().matches(".*-fanart[\\.].+");
		if (hasFileNameEnder)
			fileNameEnder = "-fanart";
		
		return fileNameEnder;
	}
	
	private String getSanitizedString(String fileName) {
		final Pattern ILLEGAL_CHARACTERS = Pattern.compile(sanitizer);
		fileName = ILLEGAL_CHARACTERS.matcher(fileName).replaceAll("").replaceAll("\\s+", " ").trim();
		return fileName;
	}
	
	public static String getAvailableTags()
	{
		String tags = "";
		for (String tag : availableRenameTags)
		{
			tags= tags + " " + tag;
		}
		return tags.trim();
	}

	public String getOldFilename() {
		return filename;
	}

	public void setOldFilename(File oldFile) {
		this.oldFile = oldFile;
	}
	
}
