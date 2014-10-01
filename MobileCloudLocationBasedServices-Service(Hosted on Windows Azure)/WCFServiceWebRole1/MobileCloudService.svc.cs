using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Blob;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;
using WCFServiceWebRole1.Model;

namespace WCFServiceWebRole1
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "Service1" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select Service1.svc or Service1.svc.cs at the Solution Explorer and start debugging.
    public class MobileCloudService : IMobileCloudService
    {
        teamcloudera_dbEntities contextDB = new teamcloudera_dbEntities();

        public string SayHello(string username)
        {
            return "Hello " + username + " welcome to the cloud computing world";
        }

        public string TestImageUrl(string username)
        {
            string status = "Image does not exist";
            var getAllUrls = (from url in contextDB.file_details
                              select url.fileurl).ToList();
            List<string> imageNames = new List<string>();
            foreach (var item in getAllUrls)
            {
                string[] names = item.ToString().Split('/');
                imageNames.Add(names[4]);
            }
            if (imageNames.Contains(username))
            {
                status = "Image exist";
            }
            return status;
        }

        public string GetImageUrl(string location, string imagename)
        {
            string connStr = ConfigurationManager.ConnectionStrings["StorageConnectionString"].ConnectionString;
            string imageurl = "";
            var filesinlocation = (from file in contextDB.file_details
                                   where file.fileloc == location
                                   select file).ToList();

            bool nearestLocationExists = false;
            foreach (var item in filesinlocation)
            {
                string[] names = item.fileurl.ToString().Split('/');
                if (names[4] == imagename)
                {
                    nearestLocationExists = true;

                }
            }
            if (nearestLocationExists)
            {
                imageurl = "https://teamcloudera.blob.core.windows.net/" + location + "/" + imagename;
            }
            else
            {
                // Retrieve storage account from connection string.
                Console.WriteLine(@"Connection to storage account");
                CloudStorageAccount storageAccount = CloudStorageAccount.Parse(connStr);

                // Create the blob client.
                CloudBlobClient blobClient = storageAccount.CreateCloudBlobClient();

                // Retrieve a reference to a container. 
                Console.WriteLine(@"Getting Reference to container");
                CloudBlobContainer container = blobClient.GetContainerReference("centralrepocontainer");

                //Now copy blob from centeral repo to phonexi repo
                CloudBlobClient targetblobClient = storageAccount.CreateCloudBlobClient();

                CloudBlobContainer targetContainer = targetblobClient.GetContainerReference(location);
                targetContainer.CreateIfNotExists();

                targetContainer.SetPermissions(
                new BlobContainerPermissions
                {
                    PublicAccess =
                        BlobContainerPublicAccessType.Container
                });
                string blobName = imagename;
                CloudBlockBlob sourceBlob = container.GetBlockBlobReference(blobName);
                CloudBlockBlob targetBlob = targetContainer.GetBlockBlobReference(blobName);
                targetBlob.StartCopyFromBlob(sourceBlob);

                //update location in database
                file_details filetoupdate = null;
                var filesnotinlocation = (from file in contextDB.file_details
                                          where file.fileloc != location
                                          select file).ToList();
                foreach (var item in filesnotinlocation)
                {
                    string[] names = item.fileurl.ToString().Split('/');
                    if (names[4] == imagename)
                    {
                        filetoupdate = item;
                    }
                }
                filetoupdate.fileloc = location;
                contextDB.Entry(filetoupdate).State = System.Data.EntityState.Modified;
                contextDB.SaveChanges();

                imageurl = "https://teamcloudera.blob.core.windows.net/" + location + "/" + blobName;
            }
            return imageurl;
        }

    }
}
