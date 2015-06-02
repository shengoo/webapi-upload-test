using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;

namespace WebApiServer.Controllers
{
    public class ValuesController : ApiController
    {
//        // GET api/values
//        public IEnumerable<string> Get()
//        {
//            return new string[] { "value1", "value2" };
//        }
//
//        // GET api/values/5
//        public string Get(int id)
//        {
//            return "value";
//        }
//
//        // POST api/values
//        public void Post([FromBody]string value)
//        {
//        }
//
//        // PUT api/values/5
//        public void Put(int id, [FromBody]string value)
//        {
//        }
//
//        // DELETE api/values/5
//        public void Delete(int id)
//        {
//        }


        public HttpResponseMessage upload()
        {
            var request = HttpContext.Current.Request;
            for (var i = 0; i < request.Files.Count; i++)
            {
                HttpPostedFile file = request.Files[i];
                using (var stream = file.InputStream)
                {
                    Stream fileStream = File.Create(HttpContext.Current.Server.MapPath("~/" + Guid.NewGuid().ToString() + file.FileName));
                    stream.CopyTo(fileStream);
                    fileStream.Close();
                    stream.Close();
                }
            }

            HttpResponseMessage response = new HttpResponseMessage();
            response.StatusCode = HttpStatusCode.Created;
            return response;
        }


    }
}
