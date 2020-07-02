# CollegeAuction

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
[Description of your app]

### App Evaluation
[Evaluation of your app across the following attributes]
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
* User can see the most current bid on an item and see the time remaining on the listing

**Optional Nice-to-have Stories**

* User can search for items by categories/hashtags
* User can "friend" other users
* User can view auction listings from their college/area
* User can see their buy/sell history
* User can favorite certain listings
* User can add a profile picture

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
    * User can buy an item
* Profile
    * User can see their buy/sell history

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Profile
* Creation

**Flow Navigation** (Screen to Screen)

* Login
   * Stream
* Register
   * Stream
* Stream
    * Detail
* Detail
    * Stream
* Creation
    * Stream
* Profile
    * Creation
    * Detail
