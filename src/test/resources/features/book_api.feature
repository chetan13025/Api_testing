@BookAPI

Feature: Books API Testing

# ===================== POST =====================

  Scenario: Create a new book
    Given I have a random book payload
    When Create Book Request
    Then the Book response status code should be 201
    And the Book response should contain "id"
    
     # ===================== GET ALL =====================
     
  Scenario: Get all Books
  	When Fetch All Books Details
  	Then the Book response status code should be 200 
  	
  # ===================== GET BY ID =====================
  
  Scenario: Get Book Details with ID 
	When  Fetch Book Details with ID 
	Then the Book response status code should be 200
	
	 # ===================== PUT =====================
	 
  Scenario: Updating Put Book details  
	When Update Book Request
	Then the Book response status code should be 200
	
	# ===================== PATCH =====================
	
  Scenario: Updating Patch Book Request 
	When Update Patch Book Request
	Then the Book response status code should be 200
	
	# ===================== DELETE =====================
	
  Scenario: Deleting Book Details
  	When Delete Book with ID
  	Then the Book response status code should be 200 
