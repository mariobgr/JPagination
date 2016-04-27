package com.mariobgr;

/**
 * @author Mario Paunov <mariobgr@abv.bg>
 */

import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Pagination {
	
	private int items = 0;
	private int itemsPerPage = 10;
	private int currentPage = 1;
	private String prevLinkTokenRegexRule = "\\[prev\\](.*)\\[\\/prev\\]";
	private String nextLinkTokenRegexRule = "\\[next\\](.*)\\[\\/next\\]";
	private String loopLinkTokenRegexRule = "\\[loop\\](.*)\\[\\/loop\\]";
	private String firstLinkTokenRegexRule = "\\[first\\](.*)\\[\\/first\\]";
	private String lastLinkTokenRegexRule = "\\[last\\](.*)\\[\\/last\\]";
	private String pagesLinkTokenRegexRule = "\\[pages\\](.*)\\[\\/pages\\]";
	private String pageFinderRegexRule = "page=([0-9]+)";
	private int pages = 0;
	
	private void Paging(int items, int itemsPerPage, int currentPage) {

		this.items = items;
		this.itemsPerPage = itemsPerPage;
		this.currentPage = currentPage;
	}
	
	private String process(HttpServletRequest req) {

		Boolean replacePrev = true;
		Boolean replaceNext = true;
		
		String templateContent = "<div class=\"btn-toolbar margin-bottom-10\">"+
			"<div class=\"btn-group\">"+
				"[first]<a class=\"btn btn-default\" href=\"#full_url#page=#first#\">&laquo;</a>[/first]"+
				"[prev]<a class=\"btn btn-default\" href=\"#full_url#page=#prev#\">&lsaquo;</a>[/prev]"+
				"[loop]<a class=\"btn btn-default\" href=\"#full_url#page=#loop#\">#loop#</a>[/loop]"+
				"[next]<a class=\"btn btn-default\" href=\"#full_url#page=#next#\">&rsaquo;</a>[/next]"+
				"[last]<a class=\"btn btn-default\" href=\"#full_url#page=#last#\">&raquo;</a>[/last]"+
			"</div>"+
		"</div>";
		
		this.pages = (int) Math.ceil((this.items / this.itemsPerPage));

		if( this.pages == 1 ) 
			return "";

		if( this.currentPage > this.pages ) {
			return "";
		}

		// We are on the first page so no need to display prev link
		if( this.currentPage == 1 ) {			
			replacePrev = false;
			templateContent = templateContent.replaceAll(this.prevLinkTokenRegexRule, "");
		}
		
		// Curent page is the last page or more so no need to display next link
		if( this.currentPage >= this.pages ) {			
			replaceNext = true;
			templateContent = templateContent.replaceAll(this.nextLinkTokenRegexRule, "");
		}
		
		

		// Check if we are in such page position so we can use prev link
		if( this.currentPage > 1 && replacePrev == true ) {
			
			Matcher matcher = Pattern.compile(this.prevLinkTokenRegexRule).matcher(templateContent);
			while(matcher.find())
				templateContent = templateContent.replaceAll(this.prevLinkTokenRegexRule, this.createPrevLink(matcher.group(1)));
			
		}

		if( this.currentPage < this.pages && replaceNext == true ) {
			
			Matcher matcher = Pattern.compile(this.nextLinkTokenRegexRule).matcher(templateContent);
			while(matcher.find())
				templateContent = templateContent.replaceAll(this.nextLinkTokenRegexRule, this.createNextLink(matcher.group(1)));
			
		}
		
		// Add first link
		if(currentPage > 1) {
			
			Matcher matcher = Pattern.compile(this.firstLinkTokenRegexRule).matcher(templateContent);
			while(matcher.find())
				templateContent = templateContent.replaceAll(this.firstLinkTokenRegexRule, this.createFirstLink(matcher.group(1)));
			
		} else
			templateContent = templateContent.replaceAll(this.firstLinkTokenRegexRule, "");
		
		// Add last link
		if(currentPage < this.pages) {
			
			Matcher matcher = Pattern.compile(this.lastLinkTokenRegexRule).matcher(templateContent);
			while(matcher.find())
				templateContent = templateContent.replaceAll(this.lastLinkTokenRegexRule, this.createLastLink(matcher.group(1)));
			
		} else
			templateContent = templateContent.replaceAll(this.lastLinkTokenRegexRule, "");
		
		String loopContent = "";

		// Get loop token
		List<String> loopMatches = preg_match(this.loopLinkTokenRegexRule, templateContent);
		
		
		int pagesPerSector = 15;
		int pagesPerSectorLeft = calcPagesPerSectorLeft( pagesPerSector );
		int start 	= 0;
		double end  = this.pages;

		if( this.pages > pagesPerSector ) {
			
			int _start = this.currentPage - pagesPerSectorLeft - 1;
			int _end   = this.currentPage + pagesPerSectorLeft;
			start  = _start > 0 ? _start : 0;
			end    = _end > this.pages ? this.pages : _end;
		}

		for( int i = start; i < end; i++ ) {

			loopContent += createLoopLink( loopMatches, i + 1 );
		}

		templateContent = templateContent.replaceAll(loopMatches.get(0), loopContent);

		// Curent page is the last page or more so no need to display count link
		if( this.currentPage + pagesPerSectorLeft >= this.pages ) {			
			replaceNext = true;
			templateContent = templateContent.replaceAll(this.pagesLinkTokenRegexRule, "");
		} else {
			
			Matcher matcher = Pattern.compile(this.pagesLinkTokenRegexRule).matcher(templateContent);
			while(matcher.find())
				templateContent = templateContent.replaceAll(this.pagesLinkTokenRegexRule, this.createPagesLink(matcher.group(1)));
			
		}
		
		templateContent = templateContent.replace("#full_url#", getURL(req));

		return templateContent.replaceAll("\\[loop\\]", "").replaceAll("\\[\\/loop\\]", "");
	}
	
	//calculate pages per sector
	private static int calcPagesPerSectorLeft( int pages ) {

		return pages % 2 == 0 ? pages / 2 : (pages - 1)/2;
	}

	//create loop
	private String createLoopLink( List<String> matches, int loop ) {
		
		String match = matches.get(0);
		
		if( loop == this.currentPage ) {
			match = match.replace("btn-default", "btn-primary");
		}
		return match.replace("#loop#", Integer.toString(loop));
	}

	//create previous link
	private String createPrevLink( String match ) {
		
		return match.replace("#prev#", Integer.toString(this.currentPage - 1));
	}

	//create next link
	private String createNextLink( String match ) {

		return match.replace("#next#", Integer.toString(this.currentPage + 1));
	}
	
	//create first link
	private String createFirstLink( String match ) {

		return match.replace("#first#", Integer.toString(1));
	}

	//create last link
	private String createLastLink( String match ) {

		return match.replace("#last#", Integer.toString((int)this.pages));
		
	}

	//create pages link
	private String createPagesLink( String match ) {
		
		return match.replace("#pages#", Double.toString(this.pages));
	}
	
	
	
	//get the full url to use for paging
	private String getURL(HttpServletRequest request) {
	    StringBuffer requestURL = request.getRequestURL();
	    String queryString = request.getQueryString();
	    String url = "";

	    if (queryString == null) {
	        url = requestURL.toString() + "?";
	    } else {
	    	url = requestURL.append('?').append(queryString).toString() + "&";
	    }
	    
	    return url.replaceAll(this.pageFinderRegexRule, "").replace("&&", "&").replace("?&","?");
	}
	
	private List<String> preg_match(String regex, String haystack) {
		
		List<String> matches = new ArrayList<String>();
		Matcher matcher = Pattern.compile(regex).matcher(haystack);
		while(matcher.find())
			matches.add(matcher.group(1));
		
		return matches;
		
	}
	
	
	
	
	
	
	
	
	// use public
	
	public static String pager(int items, int itemsPerPage, int currentPage, HttpServletRequest req) {
		
		Pagination paging = new Pagination();
		paging.Paging(items, itemsPerPage, currentPage);
		
		return paging.process(req);
		
	}

}
