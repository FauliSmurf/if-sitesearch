FAQ
=

* What is Site Search?
    * Site Search is an on-demand SaaS offering from IntraFind Software AG to enable website operators 
    to provide search functionality for their websites.

* What ist the **advantage of using Site Search** vs using search backends like *Elasticsearch* directly?
    * Site Search removes Elasticsearch as your direct dependency when introducing a search to a website. 
    In fact Site Search does not only rely on Elasticsearch but on additional components 
    from *IntraFind AG* that **improve the quality of search results**. Also Site Search is a **managed service** that 
    removes the burden of operations from your organization. 

* What is the difference between **sites** and **pages**?
    * A **site** is a *website* that can contain thousands of **pages**. 
    You may have two **sites**: *en.example.com* and *de.example.com* 
    and each may contain thousands of pages in the respective language.

* What is a **tenant**?
    * A tenant is an **organizational entity** that can manage **dozens of independent sites**.
    You may have a company that is assigned to a tenant.

* What is Site Search' primary API endpoint URL?
    * https://api.sitesearch.cloud should be used for all Site Search API calls.
    
* How can I index a website and make it searchable?
  * Currently a website needs to expose an **RSS / Atom feed** to make it easily searchable.
  This is the fastest & easiest way to get up and running. Use this ***curl command***
  
        curl -X POST https://api.sitesearch.cloud/sites/rss?feedUrl=https%3A%2F%2Fintrafind.de%2Fshare%2Fenterprise-search-blog.xml
  
  ...to [call our API accordingly](https://api.sitesearch.cloud/swagger-ui.html#!/site45controller/indexNewRssFeedUsingPOST). 
  Still you can make a website searchable that does not expose any feeds by using our API and indexing a website page by page, 
  cf. "How can I index a page and make it searchable?".  
  
* How can I **index a page**, automatically **creating a new site**, and make it searchable?
    * After executing this ***curl command***, you will also obtain the *siteId* and *siteSecret* of the newly created site. 

            curl -v -X POST \
              https://api.sitesearch.cloud/pages \
              -H 'content-type: application/json' \
              -d '{ 
                "url" : "https://example.com/page",
                "title": "Test Page",
                "body": "Test Content"
            }'

* How can I **index a page**, within an **already existing site**, and make it searchable?
    * When a page with the provided URL already exists, the page will be updated with the given title & body.
    Execute this curl command with a *siteSecret* that corresponds to the provided *siteId*:
    
            curl -v -X PUT \
              'https://api.sitesearch.cloud/sites/22d7cbe6-78c3-4f54-ba40-79b881cbe568/pages?siteSecret=8a12c8db-7d49-4f03-b2e5-03870b4e1e48' \
              -H 'content-type: application/json' \
              -d '{
                "title": "Test Page",
                "body": "Test Content",
                "url": "https://example.com/page"
            }'

* How can I **update** an already indexed page?
    * When updating a page the URL in the payload assures that only the page with the provided URL is updated with a new tile and content.
    If the page with the provided URL does not exist in a site's index, the page is automatically added to the site index.
    Execute this curl command with a *siteSecret* that corresponds to the provided *siteId*:
    
            curl -v -X PUT \
              'https://api.sitesearch.cloud/sites/22d7cbe6-78c3-4f54-ba40-79b881cbe568/pages?siteSecret=8a12c8db-7d49-4f03-b2e5-03870b4e1e48' \
              -H 'content-type: application/json' \
              -d '{
                "title": "Test Page",
                "body": "Test Content",
                "url": "https://example.com/page"
            }'

* How can I **delete** an indexed page?
    * You need to provide a **siteSecret** that corresponds to the provided **siteId**,
    as well as a page's primary ID.
    
            curl -v -X DELETE \
              https://api.sitesearch.cloud/sites/d87fdcef-a84d-462d-ba84-61df9805536a/pages/0c42f3ef01536bd29510d7b4d178fc7e6cbc1d26095ac3a759bf638f80bfa3c9?siteSecret=b554a1e7-3e87-44ab-b353-f1fd8a423bbe 

* What is a **site secret**?
    * A site secret is a token that you should keep confidential and not publish it to any public repository. 
    The site secret enables you to **add new pages and update existing pages** within a site. 
    You need to provide a *siteSecret* as a query parameter to authorize operations that modify the index of a site.

* Do I need the **search bar** to use Site Search?
    * No, not necessarily; the **search bar is a sample implementation** to quickly show you how 
    a search bar may look like and interact with the *Site Search service*.
    Still, the **search bar** is a production-grade implementation and ready for production deployments.

* Can I use my own search bar with Site Search?
    * Yes, you can use any search bar, written in any language that also runs on native platforms as long as it follows
    [Site Search' API specification](https://api.sitesearch.cloud/swagger-ui.html).

* Can I *customize* the layout and/or design of the search bar?   
    * Yes, you can *fully customize the appearance of the search bar* using HBS templates.
    * Alternative you can customize the search bar overriding its CSS classes which is appropriate for minor customizations only.

* How can I make my website searchable?
    * The easiest & quickest way is to use the [Site Search Gadget](https://api.sitesearch.cloud/sitesearch-gadget.html)
    to **create an index** from an RSS / Atom feed and make it **instantly searchable** without even embedding it into a website.
    If your website does not expose any feeds, you need to use our open [API](https://api.sitesearch.cloud/swagger-ui.html#!/page45controller/indexNewSiteUsingPOST)
    to **index your website page by page** calling the API.
    
* Where can I find an API documentation and specification for Site Search?
    * [Site Search' API](https://api.sitesearch.cloud/swagger-ui.html) ist technically documented according 
    to the OpenAPI 3.0 specification, using Swagger. Not only, you can lookup the required schemas 
    but you can also, use the API right from your browser. 

* What does Site Search cost?
    * Currently we have no fixed plans as Site Search is still in BETA. 
    As soon as Site Search meets all our production requirements, we will offer a **free basic plan** 
    and additional **enterprise offerings**.  

* How do I integrate Site Search' search bar into my website?
    * Embed the following HTML fragment into your website and **adjust the siteId** provided in the snippet:
    
        
            <link rel="stylesheet" href="https://api.sitesearch.cloud/searchbar/css/app.css"/>
            <div id="searchbarContainer">
                <style>
                    .if-teaser-highlight {
                        font-weight: bold;
                    }
                </style>
                <div class="container" style="width: 530px;">
                    <div id="searchbar"></div>
                    <div id="resultlist"></div>
                </div>
            
                <script src="https://api.sitesearch.cloud/searchbar/js/app.js"></script>
                <script>
                    jQuery.noConflict();
                    jQuery(document).ready(function ($) {
            
                        IFS.initClient({
                            sbTarget: "#searchbar",
                            configurl: "https://api.sitesearch.cloud/searchbar-config/sitesearch-config.json",
                            sitesearch: true,
                            siteId: "930bd2f4-22b6-4046-b118-9a02b5281ceb"
                        });
                    });
                </script>
            </div>  
 
* Who is using Site Search?
    * Very soon, following website's search will be powered by Site Search
        * intrafind.de 
        * intrafind.com
        * analyzelaw.com
        
    

