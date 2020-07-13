# CollegeAuction

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description

This app creates a platform for college students to but and sell items from each other.

### App Evaluation

   - **Description**: Allows students to connect and aution/buy items from each other.
   - **Category:** Marketplace
   - **Mobile:** Allows users to scroll through listings and list items to sell.
   - **Market:** Anybody who is in college and is looking to buy/sell items.
   - **Habit:** This app will be used whenever a student is trying to get rid of something or is looking for an item on the cheap.
   - **Scope:** Creates a platform that allows students to buy/sell items within their community.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can register a new account (name, college, grad year)
* User can log-in
* User can buy an item
* User can create a listing with pictures/description
* User can see their buy/sell history
* User can favorite certain listings
* A couple of sorting options for complex algorithm
* Double tap on stream to watch a product (Gesture)
* User can see the most current bid on an item and see the time remaining on the listing
* Use Maps SDK for specifying seller location & display location on product detail view
* Animation for growing when clicked on product to display detail view (Materials.io for animation)

**Optional Nice-to-have Stories**

* User can search for items by categories/hashtags
* User can "friend" other users
* User can view auction listings from their college/area
* User can add a profile picture
* User can share listings through text/email
* User can use Stripe SDK to pay/receive payment from items

### 2. Screen Archetypes

* Login
   * User can log-in
* Register
   * User can register a new account (name, college, grad year)
* Stream
    * User can see the most current bid on an item and see the time remaining on the listing
    * Items that expire the soonest are first
* Detail
    * User can buy an item
* Creation
    * User can list an item
* Profile
    * User can manage their listings

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* All items Stream
* Favorites Stream
* Profile
* Bids + Purchases
* Creation (from floating button)

**Flow Navigation** (Screen to Screen)

* Login
   * All items Stream
* Register
   * All items Stream
* Streams
    * Buyer Detail
* Detail
    * Stream
* Creation (can be accessed through the floating button)
    * Detail info input
    * Stream
* Profile
    * Seller Detail

## Wireframes
<img src="https://i.imgur.com/5OzmNaf.jpg" width=600>

## Schema 

### Models
#### Listing
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user listing (default field) |
   | seller        | Pointer to User| the user who listed the item |
   | image         | File     | image of the listed item |
   | description       | String   | description of the listed item |
   | sold     | Boolean | returns whether the item has been sold or not |
   | buyer     | Pointer | initialized to null, but is updated when the auction finishes |
   | createdAt     | DateTime | date when the listing is created (default field) |
   | updatedAt     | DateTime | date when listing is last updated (default field) |
   | favoritesCount**    | Number   | number of ppl that favorited a listing |
   ** denotes a stretch feature
   
#### User
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | profileImage         | File     | profile picture for the user |
   | phoneNumber       | String   | the contact information for buyers/sellers to connect |
   | email     | String | the contact information for buyers/sellers to connect |
   | username    | String   | how a buyer/seller is displayed to other users |
   | createdAt     | DateTime | date when the listing is created (default field) |
   | updatedAt     | DateTime | date when listing is last updated (default field) |
   
#### Bid
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | listings        | Pointer to Listing | attaches every user to their listing |
   | price         | Double     | the price that a user is willing to buy an item |
   | listing       | Pointer to a listing   | denotes which post the bid is for |
   | createdAt     | DateTime | date when the bid is submitted (default field) |
   | updatedAt     | DateTime | date when bid is last updated (default field) |
   
### Networking
#### List of network requests by screen
   - All Items Stream
      - (Read/GET) Query all listings
         ```swift
         let query = PFQuery(className:"Listing")
         query.order(byDescending: "createdAt")
         query.setLimit(20)
         query.include(Listing.KEY_USER)
         query.whereKey("sold", equalTo:false)
         query.findObjectsInBackground { (listings: [PFObject]?, error: Error?) in
            if let error = error { 
               print(error.localizedDescription)
            } else if let listings = listings {
               print("Successfully retrieved \(Listings.count) listings.")
           // TODO: Do something with posts...
            }
         }
         ```
   - Create Listing Screen
      - (Create/POST) Create a new listing object
        ```swift
        Listing listing = new Listing();
        listing.setDescription(description);
        listing.setImage(new ParseFile(photoFile));
        listing.setPrice(double initialPrice);
        listing.setUser((currentUser));
        listing.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    Log.e(TAG, "Error while saving!", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                // TODO: Do something after the item is listed
             } 
          });
        } 
   - Purchases and Listings
      - (Read/GET) Query all posts where user is author
      ```swift
      let query = PFQuery(className:"Listing")
         query.order(byDescending: "createdAt")
         query.setLimit(20)
         query.whereEqualTo("user", ParseUser.getCurrentUser())
         query.include(Listing.KEY_USER)
         query.findObjectsInBackground { (listings: [PFObject]?, error: Error?) in
            if let error = error { 
               print(error.localizedDescription)
            } else if let listings = listings {
               print("Successfully retrieved \(Listings.count) listings.")
           // TODO: Do something with posts...
            }
         }
      ```
      - (Read/GET) Update user profile image
      ``swift
      let query = PFQuery(className:"Listing")
         query.order(byDescending: "createdAt")
         query.setLimit(20)
         query.whereEqualTo("buyer", ParseUser.getCurrentUser())
         query.include(Listing.KEY_USER)
         query.findObjectsInBackground { (listings: [PFObject]?, error: Error?) in
            if let error = error { 
               print(error.localizedDescription)
            } else if let listings = listings {
               print("Successfully retrieved \(Listings.count) listings.")
           // TODO: Do something with posts...
            }
         }
      ```
   - Profile Screen
      - (Read/GET) Query logged in user object
      ```swift
      ParseUser.getCurrentUser()
      ```
      - (Update/PUT) Update user profile image
      ```swift
      user = ParseUser.getCurrentUser()
      user.putProfileImage(profile image)
      user.saveInBackground
      ``` 
