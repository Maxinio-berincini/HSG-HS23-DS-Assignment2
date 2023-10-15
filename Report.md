Assignment 1
------------

# Team Members
- Leon Luca Klaus Muscat
- Felix Kappeler
- Max Beringer

# GitHub link to your (forked) repository

> https://github.com/Maxinio-berincini/HSG-HS23-DS-Assignment2

# Task 1

1. Indicate the time necessary for the SimpleCrawler to work.

Ans:



# Task 2

1. Is the flipped index smaller or larger than the initial index? What does this depend on?

Ans:

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
> We chose the POST HTTP Method for actions that modify the data and GET for actions that retrieve data.
> 
> We also implemented messages based on the response Code from each action.

# Task 4

1.  Indicate the time necessary for the MultithreadCrawler to work.

Ans:

3. Indicate the ratio of the time for the SimpleCrawler divided by the time for the MultithreadedCrawler to get the increase in speed.

Ans:


