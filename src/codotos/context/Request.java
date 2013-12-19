package codotos.context;


/*
	This class is an representation of a Request object
	Used to access all the data about the incoming request information
*/
public class Request {


	//private $mGetData = null;
	//private $mPostData = null;
	//private $mFileData = null;
	//private $mSessionData = null;
	
	
	/*
		Setup the Request
	*/
	public Request(){
		
		// TODO
		//$this->mGetData = &$_GET;
		//$this->mPostData = &$_POST;
		//$this->mFileData = &$_FILES;
		//$this->mSessionData = &$_SESSION;
	
	}
	
	
	/*
		Returns the requested URI for the page
		
		@return String Requested URI
	*/
	public String getRequestedFilename(){
	
		// TODO TRANSLATOR
		//return $_SERVER["REDIRECT_URL"];
		return "/legitabit/";
		
	}
	
	
}

