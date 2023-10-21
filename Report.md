Assignment 2
------------

# Team Members
- Leon Luca Klaus Muscat
- Felix Kappeler
- Max Beringer

# GitHub link to your (forked) repository

> https://github.com/Maxinio-berincini/HSG-HS23-DS-Assignment2

# Task 1

1. Indicate the time necessary for the SimpleCrawler to work.

> Ans: The SimpleCrawler took 57 seconds on one of our machines.



# Task 2

1. Is the flipped index smaller or larger than the initial index? What does this depend on?

> Ans: 
> 
> The flipped index is larger, because it has the keywords as keys and the urls as values.
> As all the urls have multiple keywords, the number of keywords is larger than the number of urls.
> And because the number of repeating keywords is not that high, the number of unique keywords is much larger than the number of urls.
> So the size of the flipped index depends on the number of keywords per url and the number of unique keywords.

# Task 3

1. Explain your design choices for the API design.

> Ans:
> 
> We added the following endpoints for each admin action, to be able to keep a clear overview of all actions:
> - /admin/crawl
>   - starts crawling 
> - /admin/regenerate-flipped-index
>   - regenerates the flipped index file
> - /admin/delete-url
>   - deletes a given url from the flipped index file
> - /admin/load-url
>   - loads the keywords from a given url into the keyword field
> - /admin/update-url
>   - updates a given url with the given keywords
> 
> We grouped the endpoints into a /admin path, to make a structure that could be easily extended, without losing overview.
> 
> We chose the POST HTTP Method for actions that modify the data and GET for actions that retrieve data.
> 
> We also implemented messages based on the response Code from each action.
> Crawl and regenerate flipped index only have a success and a fail message, as they do not require any input.
> Delete url and update url have a success and a fail message, as well as a message for invalid input.
> Load url has a message for url not found in addition to the other messages.
>
> We also implemented the admin interface into the main page of our search engine.

# Task 4

1.  Indicate the time necessary for the MultithreadCrawler to work.

> Ans: 
> 
> The MultithreadCrawler took 5 seconds on one of our machines.

2. Indicate the ratio of the time for the SimpleCrawler divided by the time for the MultithreadedCrawler to get the increase in speed.

> Ans: 
> 
> 57s / 5s = 11.4, this means that the MultithreadCrawler is 11.4 times faster than the SimpleCrawler.



