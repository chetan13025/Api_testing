@Libraries

Feature: Libraries API Testing

# ===================== POST =====================

  Scenario: Create a new Library
    Given I have a random library payload
    When Create Library Request
    Then The response status code should be 201
    And The response should contain "id"
    
    
    # ===================== GET ALL =====================
    
  Scenario: Get all Libraries
  	When Fetch All Libraries Details
  	Then The response status code should be 200 
  	
  	
  	
  Scenario: Get Library Details with ID 
	When Fetch Library Details with ID
	Then The response status code should be 200
	
	# ===================== PUT =====================
	
  Scenario: Updating Put Library details  
	When Update Library Request
	Then The response status code should be 200
	
  Scenario: Updating Patch Library details  
	When Update Patch Library Request
	Then The response status code should be 200
	
	Scenario: Deleting Library details  
	When Delete Library with ID
	Then The response status code should be 200