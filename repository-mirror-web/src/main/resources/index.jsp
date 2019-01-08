<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:core="http://java.sun.com/jsp/jstl/core" version="2.0">
<html>
  <head>
    <title>Importér data</title>
  </head>
  <body>
    <div class="form-group">
      <label for="target">Vælg status</label>
      <select id="target" name="status">
	<jsp:element name="option" value="staging">
	  <jsp:attribute name="selected">true</jsp:attribute>
	  <jsp:body>TEST</jsp:body>
	</jsp:element>
	<jsp:element name="option" value="production">
	  <jsp:body>Produktion</jsp:body>
	</jsp:element>
      </select>
    </div>
  </body>
</html>
</jsp:root>