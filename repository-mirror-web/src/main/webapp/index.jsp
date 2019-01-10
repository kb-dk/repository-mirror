<jsp:root  xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:core="http://java.sun.com/jsp/jstl/core" version="2.0">
  <jsp:directive.page contentType="text/html; charset=UTF-8" />
  <html>
    <head>
      <title>Importér data til tekstportalen</title>
      <style type="text/css">
	.form-group { 
	float: left; 
	width: 30%; 
	}
      </style>
    </head>
    <body>
      <h1>Importér data til tekstportalen</h1>
      <div>
	<form method="get" action="submission/">
	  <div  class="form-group">
	    <fieldset id="status">
	      <legend for="target">Vælg destination</legend>
	      <select id="target" name="status">
		<jsp:element name="option" value="staging">
		  <jsp:attribute name="selected">true</jsp:attribute>
		  <jsp:body>Forhåndsvisning</jsp:body>
		</jsp:element>
		<jsp:element name="option" value="production">
		  <jsp:body>Publicér</jsp:body>
		</jsp:element>
	      </select>
	    </fieldset>
	  </div>
	  <div class="form-group">
	    <fieldset id="repository" >
	      <legend for="target">Vælg repositorie</legend>
	      <select id="repo">
		<jsp:element name="option" value="adl">
		  <jsp:body>Arkiv for Dansk Litteratur</jsp:body>
		</jsp:element>
		<jsp:element name="option" value="sks">
		  <jsp:body>Søren Kierkegaards Skrifter</jsp:body>
		</jsp:element>
		<jsp:element name="option" value="grundtvig">
		  <jsp:attribute name="selected">true</jsp:attribute>
		  <jsp:body>Grundtvigs Værker</jsp:body>
		</jsp:element>
		<jsp:element name="option" value="tfs">
		  <jsp:body>Trykkefrihedens Skrifter</jsp:body>
		</jsp:element>
		<jsp:element name="option" value="pmm">
		  <jsp:body>Poul Martin Møller</jsp:body>
		</jsp:element>
		<jsp:element name="option" value="holberg">
		  <jsp:body>Ludvig Holbergs Skrifter</jsp:body>
		</jsp:element>
	      </select>
	    </fieldset>
	  </div>
	  <div style="clear: both;" class="form-group" >
	    <fieldset id="submit">
	      <input id="publish-field" type="submit" name="send" value="Importér"/>
	    </fieldset>
	  </div>
	</form>
      </div>
    </body>
  </html>
</jsp:root>
