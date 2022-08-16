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
