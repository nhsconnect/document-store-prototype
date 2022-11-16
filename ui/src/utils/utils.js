export function setUrlHostToLocalHost(url) {
  const development =
    !process.env.NODE_ENV || process.env.NODE_ENV === "development";
  if (development) {
    const url_obj = new URL(url);
    url_obj.host = "localhost";
    return url_obj.toString();
  } else {
    return url;
  }
}
export const formatSize = (bytes) =>{
  const units = ['bytes', 'KB', 'MB', 'GB']
  const bytesPerKilobyte = 1024;
  const exponent = Math.floor(Math.log(bytes)/Math.log(bytesPerKilobyte));
  const divisor = Math.pow(bytesPerKilobyte, exponent);
  if(!units[exponent]){
    throw new Error("Invalid file size");
  }
  return `${Math.round(bytes/divisor)} ${units[exponent]}`;

}
