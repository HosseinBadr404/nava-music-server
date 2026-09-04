public class Music {
    private long id;
    private String title;
    private String artist;
    private String image;
    private double price;
    private int downloads;
    private double rating;
    private String category;
    private String audioBase64;

    public Music() {} 

    public Music(String title, String artist, String image, double price,
                 int downloads, double rating, String category, String audioBase64) {
        this.title = title;
        this.artist = artist;
        this.image = image;
        this.price = price;
        this.downloads = downloads;
        this.rating = rating;
        this.category = category;
        this.audioBase64 = audioBase64;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getDownloads() { return downloads; }
    public void setDownloads(int downloads) { this.downloads = downloads; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAudioBase64() { return audioBase64; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }

    public boolean isFree() { return price <= 0; }
    public void incrementDownloads() { this.downloads++; }
}