/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import java.awt.Image;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;
import org.json.JSONArray;
/**
 *
 * @author USER
 */
public class AplikasiCekCuaca extends javax.swing.JFrame {

        private static final String API_KEY = "fcbc2f5243d80803af8f06082c8e6149";
        private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
        private DefaultTableModel tableModel;
        private ArrayList<String> favoriteCities;
        private String csvFilePath = "weather_history.csv";
        
    public AplikasiCekCuaca() {
        initComponents();
        initializeTable();
        initializeFavorites();
    }

    private void initializeTable() {
    String[] columns = {"Waktu", "Kota", "Suhu (°C)", "Deskripsi", "Kelembaban (%)"};
    tableModel = new DefaultTableModel(columns, 0);
    tblWeatherHistory.setModel(tableModel);
}
    
    private void initializeFavorites() {
    favoriteCities = new ArrayList<>();
    loadFavoritesFromFile();
    updateComboBox();
}

private void loadFavoritesFromFile() {
    try {
        File file = new File("favorites.txt");
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                favoriteCities.add(line);
            }
            reader.close();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void updateComboBox() {
    cmbFavorite.removeAllItems();
    cmbFavorite.addItem("-- Pilih Kota Favorit --");
    for (String city : favoriteCities) {
        cmbFavorite.addItem(city);
    }
}

    private void checkWeather() {
    String city = txtCity.getText().trim();
    
    if (city.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan nama kota!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        // Buat URL dengan parameter
        String urlString = API_URL + "?q=" + URLEncoder.encode(city, "UTF-8") 
                          + "&appid=" + API_KEY + "&units=metric&lang=id";
        
        // Buat koneksi
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        // Baca response
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        // Parse JSON
        JSONObject json = new JSONObject(response.toString());
        
        // Ambil data
        String cityName = json.getString("name");
        double temp = json.getJSONObject("main").getDouble("temp");
        int humidity = json.getJSONObject("main").getInt("humidity");
        double windSpeed = json.getJSONObject("wind").getDouble("speed");
        
        JSONArray weatherArray = json.getJSONArray("weather");
        String description = weatherArray.getJSONObject(0).getString("description");
        String main = weatherArray.getJSONObject(0).getString("main");
        
        // Update UI
        displayWeatherData(cityName, temp, description, humidity, windSpeed, main);
        
        // Tambahkan ke tabel
        addToTable(cityName, temp, description, humidity);
        
        // OTOMATIS SIMPAN KE FAVORIT
        autoSaveToFavorite(cityName);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal mengambil data cuaca: " + e.getMessage(), 
                                      "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private void displayWeatherData(String city, double temp, String desc, int humidity, double wind, String mainWeather) {
    lblCityName.setText("Kota: " + city);
    lblTemperature.setText("Suhu: " + String.format("%.1f", temp) + " °C");
    lblDescription.setText("Kondisi: " + desc);
    lblHumidity.setText("Kelembaban: " + humidity + "%");
    lblWindSpeed.setText("Kecepatan Angin: " + wind + " m/s");
    
    // Set icon
    setWeatherIcon(mainWeather);
}

private void setWeatherIcon(String condition) {
    String iconPath = "images/";
    
    switch (condition.toLowerCase()) {
        case "clear":
            iconPath += "pngegg.png";
            break;
        case "clouds":
            iconPath += "Clouds.png";
            break;
        case "rain":
            iconPath += "rain.png";
            break;
        case "drizzle":
            iconPath += "drizzle.png";
            break;
        case "thunderstorm":
            iconPath += "thunderstorm.png";
            break;
        case "snow":
            iconPath += "snow.png";
            break;
        case "mist":
        case "fog":
        case "haze":
            iconPath += "mist.png";
            break;
        default:
            iconPath += "clear.png";
    }
    
    try {
        ImageIcon icon = new ImageIcon(getClass().getResource("/" + iconPath));
        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        lblWeatherIcon.setIcon(new ImageIcon(img));
    } catch (Exception e) {
        System.out.println("Gambar tidak ditemukan: " + iconPath);
    }
}

    private void addToTable(String city, double temp, String desc, int humidity) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    String time = sdf.format(new Date());
    
    Object[] row = {time, city, String.format("%.1f", temp), desc, humidity};
    tableModel.addRow(row);
}
    
    private void saveFavorite() {
    String city = txtCity.getText().trim();
    
    if (city.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan nama kota!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    if (favoriteCities.contains(city)) {
        JOptionPane.showMessageDialog(this, "Kota sudah ada di favorit!", "Info", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    favoriteCities.add(city);
    saveFavoritesToFile();
    updateComboBox();
    JOptionPane.showMessageDialog(this, "Kota berhasil ditambahkan ke favorit!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
}

private void saveFavoritesToFile() {
    try {
        BufferedWriter writer = new BufferedWriter(new FileWriter("favorites.txt"));
        for (String city : favoriteCities) {
            writer.write(city);
            writer.newLine();
        }
        writer.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void deleteFavorite() {
    String selected = (String) cmbFavorite.getSelectedItem();
    
    if (selected == null || selected.equals("-- Pilih Kota Favorit --")) {
        JOptionPane.showMessageDialog(this, "Pilih kota yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    favoriteCities.remove(selected);
    saveFavoritesToFile();
    updateComboBox();
    JOptionPane.showMessageDialog(this, "Kota berhasil dihapus dari favorit!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
}

    private void saveToCSV() {
    try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
        
        // Tulis header
        writer.write("Waktu,Kota,Suhu,Deskripsi,Kelembaban");
        writer.newLine();
        
        // Tulis data dari tabel
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write(tableModel.getValueAt(i, j).toString());
                if (j < tableModel.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();
        }
        
        writer.close();
        JOptionPane.showMessageDialog(this, "Data berhasil disimpan ke CSV!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void loadFromCSV() {
    try {
        File file = new File(csvFilePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File CSV tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Hapus data lama di tabel
        tableModel.setRowCount(0);
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        boolean firstLine = true;
        
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue; // Skip header
            }
            
            String[] data = line.split(",");
            tableModel.addRow(data);
        }
        
        reader.close();
        JOptionPane.showMessageDialog(this, "Data berhasil dimuat dari CSV!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void autoSaveToFavorite(String city) {
    // Cek apakah kota sudah ada di favorit
    if (!favoriteCities.contains(city)) {
        favoriteCities.add(city);
        saveFavoritesToFile();
        updateComboBox();
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelUtama = new javax.swing.JPanel();
        PanelJudul = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        PanelInputKota = new javax.swing.JPanel();
        lblCity = new javax.swing.JLabel();
        txtCity = new javax.swing.JTextField();
        btnCheck = new javax.swing.JButton();
        btnSaveFavorite = new javax.swing.JButton();
        PanelKotaFavorit = new javax.swing.JPanel();
        lblFavorite = new javax.swing.JLabel();
        cmbFavorite = new javax.swing.JComboBox<>();
        btnDeleteFavorite = new javax.swing.JButton();
        PanelInformasiCuaca = new javax.swing.JPanel();
        lblWeatherIcon = new javax.swing.JLabel();
        lblCityName = new javax.swing.JLabel();
        lblTemperature = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();
        lblHumidity = new javax.swing.JLabel();
        lblWindSpeed = new javax.swing.JLabel();
        PanelPengecekanCuaca = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblWeatherHistory = new javax.swing.JTable();
        PanelData = new javax.swing.JPanel();
        btnSaveToCSV = new javax.swing.JButton();
        btnLoadFromCSV = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PanelUtama.setBackground(new java.awt.Color(0, 0, 0));

        PanelJudul.setBackground(new java.awt.Color(0, 204, 204));

        lblJudul.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblJudul.setText("APLIKASI CEK CUACA");

        javax.swing.GroupLayout PanelJudulLayout = new javax.swing.GroupLayout(PanelJudul);
        PanelJudul.setLayout(PanelJudulLayout);
        PanelJudulLayout.setHorizontalGroup(
            PanelJudulLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelJudulLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblJudul)
                .addGap(325, 325, 325))
        );
        PanelJudulLayout.setVerticalGroup(
            PanelJudulLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelJudulLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(lblJudul)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        PanelInputKota.setBackground(new java.awt.Color(0, 102, 102));
        PanelInputKota.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input Kota", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        lblCity.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblCity.setForeground(new java.awt.Color(255, 255, 255));
        lblCity.setText("Masukkan Kota :");

        btnCheck.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCheck.setText("Cek Cuaca");
        btnCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckActionPerformed(evt);
            }
        });

        btnSaveFavorite.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSaveFavorite.setText("Simpan Ke Favorit");
        btnSaveFavorite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveFavoriteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelInputKotaLayout = new javax.swing.GroupLayout(PanelInputKota);
        PanelInputKota.setLayout(PanelInputKotaLayout);
        PanelInputKotaLayout.setHorizontalGroup(
            PanelInputKotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelInputKotaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelInputKotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCity)
                    .addGroup(PanelInputKotaLayout.createSequentialGroup()
                        .addComponent(lblCity, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(btnCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSaveFavorite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PanelInputKotaLayout.setVerticalGroup(
            PanelInputKotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelInputKotaLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(lblCity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCity, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveFavorite)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanelKotaFavorit.setBackground(new java.awt.Color(0, 102, 102));
        PanelKotaFavorit.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Kota Favorit", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        lblFavorite.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFavorite.setForeground(new java.awt.Color(255, 255, 255));
        lblFavorite.setText("Pilih Kota Favorit :");

        cmbFavorite.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cmbFavorite.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Kota Favorit", " " }));
        cmbFavorite.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbFavoriteItemStateChanged(evt);
            }
        });

        btnDeleteFavorite.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteFavorite.setText("Hapus Favorit");
        btnDeleteFavorite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteFavoriteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelKotaFavoritLayout = new javax.swing.GroupLayout(PanelKotaFavorit);
        PanelKotaFavorit.setLayout(PanelKotaFavoritLayout);
        PanelKotaFavoritLayout.setHorizontalGroup(
            PanelKotaFavoritLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelKotaFavoritLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFavorite, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(PanelKotaFavoritLayout.createSequentialGroup()
                .addGroup(PanelKotaFavoritLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbFavorite, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteFavorite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PanelKotaFavoritLayout.setVerticalGroup(
            PanelKotaFavoritLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelKotaFavoritLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblFavorite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbFavorite, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteFavorite)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanelInformasiCuaca.setBackground(new java.awt.Color(0, 102, 102));
        PanelInformasiCuaca.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Informasi Cuaca", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        lblWeatherIcon.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblWeatherIcon.setForeground(new java.awt.Color(255, 255, 255));
        lblWeatherIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWeatherIcon.setText("Icon");

        lblCityName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblCityName.setForeground(new java.awt.Color(255, 255, 255));
        lblCityName.setText("Kota :");

        lblTemperature.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblTemperature.setForeground(new java.awt.Color(255, 255, 255));
        lblTemperature.setText("Suhu :");

        lblDescription.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDescription.setForeground(new java.awt.Color(255, 255, 255));
        lblDescription.setText("Kondisi :");

        lblHumidity.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblHumidity.setForeground(new java.awt.Color(255, 255, 255));
        lblHumidity.setText("Kelembaban :");

        lblWindSpeed.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblWindSpeed.setForeground(new java.awt.Color(255, 255, 255));
        lblWindSpeed.setText("Kecepatan Angin :");

        javax.swing.GroupLayout PanelInformasiCuacaLayout = new javax.swing.GroupLayout(PanelInformasiCuaca);
        PanelInformasiCuaca.setLayout(PanelInformasiCuacaLayout);
        PanelInformasiCuacaLayout.setHorizontalGroup(
            PanelInformasiCuacaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelInformasiCuacaLayout.createSequentialGroup()
                .addGroup(PanelInformasiCuacaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelInformasiCuacaLayout.createSequentialGroup()
                        .addGap(168, 168, 168)
                        .addComponent(lblWeatherIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelInformasiCuacaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblCityName, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelInformasiCuacaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelInformasiCuacaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(PanelInformasiCuacaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblWindSpeed, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblHumidity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblDescription, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(187, Short.MAX_VALUE))
        );
        PanelInformasiCuacaLayout.setVerticalGroup(
            PanelInformasiCuacaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelInformasiCuacaLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblWeatherIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblCityName)
                .addGap(18, 18, 18)
                .addComponent(lblTemperature)
                .addGap(18, 18, 18)
                .addComponent(lblDescription)
                .addGap(18, 18, 18)
                .addComponent(lblHumidity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblWindSpeed)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanelPengecekanCuaca.setBackground(new java.awt.Color(0, 102, 102));
        PanelPengecekanCuaca.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Riwayat Pengecekan Cuaca", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        tblWeatherHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Waktu", "Kota", "Suhu", "Deskripsi", "Kelembaban"
            }
        ));
        jScrollPane1.setViewportView(tblWeatherHistory);

        javax.swing.GroupLayout PanelPengecekanCuacaLayout = new javax.swing.GroupLayout(PanelPengecekanCuaca);
        PanelPengecekanCuaca.setLayout(PanelPengecekanCuacaLayout);
        PanelPengecekanCuacaLayout.setHorizontalGroup(
            PanelPengecekanCuacaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelPengecekanCuacaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanelPengecekanCuacaLayout.setVerticalGroup(
            PanelPengecekanCuacaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelPengecekanCuacaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanelData.setBackground(new java.awt.Color(0, 102, 102));
        PanelData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data CSV", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        btnSaveToCSV.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSaveToCSV.setText("Simpan Ke CSV");
        btnSaveToCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveToCSVActionPerformed(evt);
            }
        });

        btnLoadFromCSV.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLoadFromCSV.setText("Muat Dari CSV");
        btnLoadFromCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadFromCSVActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelDataLayout = new javax.swing.GroupLayout(PanelData);
        PanelData.setLayout(PanelDataLayout);
        PanelDataLayout.setHorizontalGroup(
            PanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSaveToCSV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLoadFromCSV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PanelDataLayout.setVerticalGroup(
            PanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelDataLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSaveToCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLoadFromCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PanelUtamaLayout = new javax.swing.GroupLayout(PanelUtama);
        PanelUtama.setLayout(PanelUtamaLayout);
        PanelUtamaLayout.setHorizontalGroup(
            PanelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelJudul, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(PanelUtamaLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(PanelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(PanelInputKota, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PanelKotaFavorit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PanelInformasiCuaca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PanelPengecekanCuaca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PanelData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PanelUtamaLayout.setVerticalGroup(
            PanelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelUtamaLayout.createSequentialGroup()
                .addComponent(PanelJudul, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(PanelUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelUtamaLayout.createSequentialGroup()
                        .addComponent(PanelInputKota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(PanelKotaFavorit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(PanelInformasiCuaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelUtamaLayout.createSequentialGroup()
                        .addComponent(PanelPengecekanCuaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PanelData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelUtama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(PanelUtama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckActionPerformed
        checkWeather();
    }//GEN-LAST:event_btnCheckActionPerformed

    private void btnSaveFavoriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveFavoriteActionPerformed
         saveFavorite();
    }//GEN-LAST:event_btnSaveFavoriteActionPerformed

    private void btnDeleteFavoriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteFavoriteActionPerformed
        deleteFavorite();
    }//GEN-LAST:event_btnDeleteFavoriteActionPerformed

    private void btnSaveToCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveToCSVActionPerformed
        saveToCSV();
    }//GEN-LAST:event_btnSaveToCSVActionPerformed

    private void btnLoadFromCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadFromCSVActionPerformed
        loadFromCSV();
    }//GEN-LAST:event_btnLoadFromCSVActionPerformed

    private void cmbFavoriteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbFavoriteItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selected = (String) cmbFavorite.getSelectedItem();
        if (selected != null && !selected.equals("-- Pilih Kota Favorit --")) {
            txtCity.setText(selected);
            checkWeather();
        }
    }
    }//GEN-LAST:event_cmbFavoriteItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AplikasiCekCuaca.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AplikasiCekCuaca.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AplikasiCekCuaca.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiCekCuaca.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AplikasiCekCuaca().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelData;
    private javax.swing.JPanel PanelInformasiCuaca;
    private javax.swing.JPanel PanelInputKota;
    private javax.swing.JPanel PanelJudul;
    private javax.swing.JPanel PanelKotaFavorit;
    private javax.swing.JPanel PanelPengecekanCuaca;
    private javax.swing.JPanel PanelUtama;
    private javax.swing.JButton btnCheck;
    private javax.swing.JButton btnDeleteFavorite;
    private javax.swing.JButton btnLoadFromCSV;
    private javax.swing.JButton btnSaveFavorite;
    private javax.swing.JButton btnSaveToCSV;
    private javax.swing.JComboBox<String> cmbFavorite;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCity;
    private javax.swing.JLabel lblCityName;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblFavorite;
    private javax.swing.JLabel lblHumidity;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblTemperature;
    private javax.swing.JLabel lblWeatherIcon;
    private javax.swing.JLabel lblWindSpeed;
    private javax.swing.JTable tblWeatherHistory;
    private javax.swing.JTextField txtCity;
    // End of variables declaration//GEN-END:variables
}
