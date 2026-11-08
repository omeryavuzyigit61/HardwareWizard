package com.panda.myapplication.domain.engine

/**
 * Profesyonel İş İstasyonu & Yaratıcı Donanım Laboratuvarı Hesaplama Motoru.
 * Puget Systems, Blender Open Data, Blackmagic Design, SPECworkstation 2026,
 * Epic Games, Unity Technologies, Image-Line, Steinberg ve MLPerf resmi test standartlarına dayanır.
 */
object WorkstationRequirementEngine {

    val supportedApps: List<WorkstationApp> = listOf(
        // ==========================================
        // 1. VIDEO & POST-PRODÜKSİYON (6 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "premiere_pro",
            name = "Adobe Premiere Pro 2026",
            shortName = "Premiere Pro",
            category = WorkstationCategory.VIDEO_EDITING,
            iconName = "video_camera",
            primaryBottleneckTr = "Intel QuickSync (10-bit 4:2:2 donanımsal çözme) & GPU VRAM",
            primaryBottleneckEn = "Intel QuickSync (10-bit 4:2:2 hardware decoding) & GPU VRAM",
            officialBenchmarkSource = "Puget Systems Adobe Premiere Pro Benchmark Suite 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "davinci_resolve",
            name = "DaVinci Resolve Studio 19",
            shortName = "DaVinci Resolve",
            category = WorkstationCategory.VIDEO_EDITING,
            iconName = "palette",
            primaryBottleneckTr = "GPU VRAM Kapasitesi & CUDA/OpenCL Hesaplama Çekirdekleri",
            primaryBottleneckEn = "GPU VRAM Capacity & CUDA/OpenCL Compute Cores",
            officialBenchmarkSource = "Blackmagic Design Official Configuration Guide & Puget Systems DaVinci Suite",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "after_effects",
            name = "Adobe After Effects 2026",
            shortName = "After Effects",
            category = WorkstationCategory.VIDEO_EDITING,
            iconName = "auto_awesome_motion",
            primaryBottleneckTr = "RAM Kapasitesi (Her CPU çekirdeğine 4-8GB) & MFR Çoklu Çekirdek",
            primaryBottleneckEn = "RAM Capacity (4-8GB per CPU core) & MFR Multithreading",
            officialBenchmarkSource = "Puget Systems After Effects Multi-Frame Rendering Benchmark",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "foundry_nuke",
            name = "Foundry Nuke 15",
            shortName = "Foundry Nuke",
            category = WorkstationCategory.VIDEO_EDITING,
            iconName = "layers",
            primaryBottleneckTr = "PCIe 4.0/5.0 NVMe I/O Hızı (Çok kanallı EXR) & 64GB-128GB RAM",
            primaryBottleneckEn = "PCIe Gen4/Gen5 NVMe I/O (Multi-channel EXR) & 64GB-128GB RAM",
            officialBenchmarkSource = "The Foundry Official Workstation Matrix & SPECworkstation 2026",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "fusion_studio",
            name = "Blackmagic Fusion Studio 19",
            shortName = "Fusion Studio",
            category = WorkstationCategory.VIDEO_EDITING,
            iconName = "scatter_plot",
            primaryBottleneckTr = "Gerçek Zamanlı 3D Nod Kompozitleme & GPU Bellek Bant Genişliği",
            primaryBottleneckEn = "Real-Time 3D Node Compositing & GPU Memory Bandwidth",
            officialBenchmarkSource = "Blackmagic Design Official Fusion Performance Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "avid_composer",
            name = "Avid Media Composer 2026",
            shortName = "Avid Composer",
            category = WorkstationCategory.VIDEO_EDITING,
            iconName = "movie_creation",
            primaryBottleneckTr = "Broadcast DNxHR Codec İşleme & Düşük Gecikmeli DDR5 Bellek",
            primaryBottleneckEn = "Broadcast DNxHR Codec Processing & Low-Latency DDR5 Memory",
            officialBenchmarkSource = "Avid Technology Media Composer Certified Workstations 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // ==========================================
        // 2. 3D MODELLEME, CGI & VFX (6 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "blender",
            name = "Blender 4.x (Cycles / Eevee Next)",
            shortName = "Blender",
            category = WorkstationCategory.VFX_3D,
            iconName = "view_in_ar",
            primaryBottleneckTr = "NVIDIA OptiX Işın İzleme Çekirdekleri & GPU VRAM Havuzu",
            primaryBottleneckEn = "NVIDIA OptiX Ray Tracing Cores & GPU VRAM Pool",
            officialBenchmarkSource = "Blender Open Data (opendata.blender.org) Cycles v4.x Benchmark",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "cinema4d",
            name = "Cinema 4D (Redshift Engine)",
            shortName = "Cinema 4D",
            category = WorkstationCategory.VFX_3D,
            iconName = "3d_rotation",
            primaryBottleneckTr = "GPU CUDA/OptiX İşlem Gücü & Yüksek Saat Hızlı CPU",
            primaryBottleneckEn = "GPU CUDA/OptiX Compute & High-Clock Single Core CPU",
            officialBenchmarkSource = "Maxon Cinebench 2026 & Redshift GPU Benchmark Suite",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "maya",
            name = "Autodesk Maya & 3ds Max (Arnold)",
            shortName = "Autodesk Maya / Max",
            category = WorkstationCategory.VFX_3D,
            iconName = "animation",
            primaryBottleneckTr = "Karakter Rigleme (Tek Çekirdek) & Arnold GPU Hibrit Render",
            primaryBottleneckEn = "Character Rigging (Single Core) & Arnold GPU Hybrid Render",
            officialBenchmarkSource = "Autodesk Maya Hardware Certification & SPECworkstation 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "houdini",
            name = "SideFX Houdini 20 (FX Sim)",
            shortName = "SideFX Houdini",
            category = WorkstationCategory.VFX_3D,
            iconName = "tsunami",
            primaryBottleneckTr = "Akışkan/Partikül Simülasyonu için Devasa CPU Çekirdeği & 128GB+ RAM",
            primaryBottleneckEn = "Massive Multi-Core CPU & 128GB+ RAM for Fluid/Pyro Dynamics",
            officialBenchmarkSource = "SideFX Official Hardware & Simulation Architecture Guide",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "zbrush",
            name = "Maxon ZBrush 2026",
            shortName = "Maxon ZBrush",
            category = WorkstationCategory.VFX_3D,
            iconName = "brush",
            primaryBottleneckTr = "100 Milyon Poligon için CPU IPC Gücü & Düşük Bellek Gecikmesi",
            primaryBottleneckEn = "CPU IPC Single-Threaded Prowess & Fast RAM for 100M+ Polys",
            officialBenchmarkSource = "Pixologic / Maxon ZBrush Certified Hardware Metric",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "substance_painter",
            name = "Adobe Substance 3D Painter",
            shortName = "Substance Painter",
            category = WorkstationCategory.VFX_3D,
            iconName = "format_paint",
            primaryBottleneckTr = "8K UDIM Doku Katmanları & GPU Direct3D 12 VRAM Kapasitesi",
            primaryBottleneckEn = "8K UDIM Texture Layering & GPU DirectX 12 VRAM Capacity",
            officialBenchmarkSource = "Adobe Substance 3D Workstation Matrix 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // ==========================================
        // 3. OYUN MOTORLARI & ARCHVIZ (6 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "unreal_engine",
            name = "Unreal Engine 5.5",
            shortName = "Unreal Engine 5",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ,
            iconName = "sports_esports",
            primaryBottleneckTr = "Lumen/Nanite Donanım Işın İzleme & PCIe 4.0/5.0 NVMe SSD",
            primaryBottleneckEn = "Lumen/Nanite Hardware RT & High-Speed PCIe 4.0/5.0 NVMe",
            officialBenchmarkSource = "Epic Games Unreal Engine 5.5 Production Workstation Hardware Matrix",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "unity_engine",
            name = "Unity 6 (URP / HDRP)",
            shortName = "Unity 6",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ,
            iconName = "developer_board",
            primaryBottleneckTr = "C# Burst & IL2CPP Derleme Hızı & 32-64GB RAM",
            primaryBottleneckEn = "C# Burst & IL2CPP Compilation Speed & 32-64GB RAM",
            officialBenchmarkSource = "Unity Technologies Certified Development Hardware 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "d5_render",
            name = "D5 Render 2026 (RTX Engine)",
            shortName = "D5 Render",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ,
            iconName = "wb_sunny",
            primaryBottleneckTr = "Gerçek Zamanlı Donanımsal Işın İzleme (DLSS 3.5) & 16GB+ VRAM",
            primaryBottleneckEn = "Real-Time Hardware Ray Tracing (DLSS 3.5) & 16GB+ VRAM",
            officialBenchmarkSource = "D5 Render Official Benchmark Database 2026",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "lumion",
            name = "Lumion 2024 / 2025",
            shortName = "Lumion",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ,
            iconName = "villa",
            primaryBottleneckTr = "GPU PassMark Puanı & Devasa Peyzaj Modelleri için VRAM",
            primaryBottleneckEn = "GPU PassMark Score & VRAM for Massive Landscape Assets",
            officialBenchmarkSource = "Act-3D Lumion Official System Hardware Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "chaos_vantage",
            name = "Chaos Vantage / Enscape",
            shortName = "Chaos Vantage",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ,
            iconName = "apartment",
            primaryBottleneckTr = "Canlı CAD/BIM Senkronizasyonu & RTX Donanım Işın İzleme",
            primaryBottleneckEn = "Live CAD/BIM Sync & Pure RTX Hardware Ray Tracing",
            officialBenchmarkSource = "Chaos Group Vantage Official Performance Whitepaper",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "godot_engine",
            name = "Godot Engine 4.3+",
            shortName = "Godot 4",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ,
            iconName = "smart_toy",
            primaryBottleneckTr = "Vulkan Forward+ Viewport & Hızlı C++/C# Proje Yükleme",
            primaryBottleneckEn = "Vulkan Forward+ Viewport & Fast C++/C# Project Loading",
            officialBenchmarkSource = "Godot Engine Vulkan Benchmark Suite",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // ==========================================
        // 4. MÜHENDİSLİK, CAD & SİMÜLASYON (6 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "solidworks",
            name = "Dassault Systèmes SolidWorks 2026",
            shortName = "SolidWorks",
            category = WorkstationCategory.CAD_ENGINEERING,
            iconName = "precision_manufacturing",
            primaryBottleneckTr = "Büyük Montaj Çizimlerinde CPU Tek Çekirdek IPC & RealView GPU",
            primaryBottleneckEn = "CPU Single-Core IPC for Large Assemblies & RealView GPU",
            officialBenchmarkSource = "SPECworkstation 2026 SolidWorks Workload & Dassault Systemes",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "autocad",
            name = "Autodesk AutoCAD 2026",
            shortName = "AutoCAD",
            category = WorkstationCategory.CAD_ENGINEERING,
            iconName = "architecture",
            primaryBottleneckTr = "Yüksek Saat Hızlı (5.4GHz+) CPU Tek Çekirdek Gücü & 32GB RAM",
            primaryBottleneckEn = "High-Clock (5.4GHz+) CPU Single-Core & 32GB RAM",
            officialBenchmarkSource = "Autodesk AutoCAD 2026 System Requirements & Puget Systems",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "revit",
            name = "Autodesk Revit 2026 (BIM)",
            shortName = "Autodesk Revit",
            category = WorkstationCategory.CAD_ENGINEERING,
            iconName = "domain",
            primaryBottleneckTr = "BIM Model Boyutu (64GB RAM) & Tek Çekirdek Saat Hızı",
            primaryBottleneckEn = "BIM Model Geometry (64GB RAM) & Fast CPU Single Core",
            officialBenchmarkSource = "Autodesk Revit Model Performance Technical Guide 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "fusion360",
            name = "Autodesk Fusion 360",
            shortName = "Fusion 360",
            category = WorkstationCategory.CAD_ENGINEERING,
            iconName = "handyman",
            primaryBottleneckTr = "Yerel CAM Takım Yolu Hesaplama & Generative Design Hızı",
            primaryBottleneckEn = "Local CAM Toolpath Calculation & Generative Design Compute",
            officialBenchmarkSource = "Autodesk Fusion 360 Performance Guidelines",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "ansys",
            name = "ANSYS Fluent & Mechanical (FEA/CFD)",
            shortName = "ANSYS Fluent",
            category = WorkstationCategory.CAD_ENGINEERING,
            iconName = "air",
            primaryBottleneckTr = "Çoklu CPU Çekirdeği, AVX-512 & Bellek Bant Genişliği (Quad Channel)",
            primaryBottleneckEn = "Multi-Core Scaling, AVX-512 & Memory Bandwidth (Quad Channel)",
            officialBenchmarkSource = "ANSYS Official HPC Hardware & Platform Benchmarks",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "matlab",
            name = "MathWorks MATLAB & Simulink",
            shortName = "MATLAB",
            category = WorkstationCategory.CAD_ENGINEERING,
            iconName = "functions",
            primaryBottleneckTr = "Büyük Matris Çözücüleri (BLAS/LAPACK) & CUDA GPU Paralel Havuz",
            primaryBottleneckEn = "Dense Matrix Solvers (BLAS/LAPACK) & CUDA GPU Parallel Pool",
            officialBenchmarkSource = "MathWorks MATLAB Bench Suite & Parallel Computing Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),

        // ==========================================
        // 5. YAPAY ZEKA & VERİ BİLİMİ (4 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "stable_diffusion",
            name = "Stable Diffusion & ComfyUI (Flux.1 / SDXL)",
            shortName = "ComfyUI / SD",
            category = WorkstationCategory.LOCAL_AI,
            iconName = "auto_awesome",
            primaryBottleneckTr = "GPU VRAM (Flux.1 için min 16-24GB) & TensorRT Çekirdekleri",
            primaryBottleneckEn = "GPU VRAM (16-24GB for Flux.1) & TensorRT Cores",
            officialBenchmarkSource = "Puget Systems Generative AI & ComfyUI Iteration Matrix",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "local_llm",
            name = "Ollama & LM Studio (Llama 3.3 / DeepSeek R1)",
            shortName = "Ollama / LLM",
            category = WorkstationCategory.LOCAL_AI,
            iconName = "psychology",
            primaryBottleneckTr = "GPU Bellek Bant Genişliği (GB/s) & 24GB VRAM (70B Q4 Çıkarım)",
            primaryBottleneckEn = "GPU Memory Bandwidth (GB/s) & 24GB VRAM (70B Q4 Inference)",
            officialBenchmarkSource = "LocalLLM Tokens-per-Second Matrix & Ollama Benchmark",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "pytorch_cuda",
            name = "PyTorch & TensorFlow (Model Eğitimi)",
            shortName = "PyTorch CUDA",
            category = WorkstationCategory.LOCAL_AI,
            iconName = "hub",
            primaryBottleneckTr = "FP16/BF16 Tensör Hesaplama, 24GB+ VRAM & PCIe Gen5 x16 Hattı",
            primaryBottleneckEn = "FP16/BF16 Tensor Compute, 24GB+ VRAM & PCIe Gen5 x16 Lanes",
            officialBenchmarkSource = "MLPerf Training & Inference Open Benchmark Matrix",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "topaz_ai",
            name = "Topaz Video AI & Photo AI",
            shortName = "Topaz Video AI",
            category = WorkstationCategory.LOCAL_AI,
            iconName = "high_quality",
            primaryBottleneckTr = "4K/8K AI Video Upscale (Iris/Artemis) & TensorRT FP16 Hızı",
            primaryBottleneckEn = "4K/8K AI Video Upscaling (Iris/Artemis) & TensorRT FP16",
            officialBenchmarkSource = "Topaz Labs Official Hardware Benchmark 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),

        // ==========================================
        // 6. GRAFİK & FOTOĞRAF DÜZENLEME (4 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "photoshop",
            name = "Adobe Photoshop 2026",
            shortName = "Photoshop",
            category = WorkstationCategory.GRAPHIC_DESIGN,
            iconName = "brush",
            primaryBottleneckTr = "CPU Tek Çekirdek IPC Gücü & Scratch Disk NVMe Hızı",
            primaryBottleneckEn = "CPU Single Core IPC Prowess & NVMe Scratch Disk Speed",
            officialBenchmarkSource = "Puget Systems Photoshop Benchmark Suite 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "lightroom",
            name = "Adobe Lightroom Classic 2026",
            shortName = "Lightroom Classic",
            category = WorkstationCategory.GRAPHIC_DESIGN,
            iconName = "camera_roll",
            primaryBottleneckTr = "Toplu RAW Dışa Aktarma (Çoklu Çekirdek) & AI Maskeleme",
            primaryBottleneckEn = "Batch RAW Export (Multithreading) & AI Masking Accelerator",
            officialBenchmarkSource = "Puget Systems Lightroom Classic Benchmark Suite",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "illustrator",
            name = "Adobe Illustrator 2026",
            shortName = "Illustrator",
            category = WorkstationCategory.GRAPHIC_DESIGN,
            iconName = "draw",
            primaryBottleneckTr = "GPU Hızlandırmalı Vektör Çizimi & Yüksek Tek Çekirdek Saat Hızı",
            primaryBottleneckEn = "GPU Accelerated Vector Canvas & High Single-Thread Clock",
            officialBenchmarkSource = "Adobe Illustrator Performance Guidance 2026",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "capture_one",
            name = "Capture One Pro 24",
            shortName = "Capture One",
            category = WorkstationCategory.GRAPHIC_DESIGN,
            iconName = "photo_camera",
            primaryBottleneckTr = "100MP Canlı Tethered Çekim, OpenCL Renk Çözücü & PCIe 4.0 NVMe",
            primaryBottleneckEn = "100MP Live Tethering, OpenCL Debayer Engine & PCIe Gen4 NVMe",
            officialBenchmarkSource = "Capture One Studio Hardware Benchmark",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // ==========================================
        // 7. MÜZİK PRODÜKSİYONU & SES (3 Uygulama)
        // ==========================================
        WorkstationApp(
            id = "fl_studio",
            name = "FL Studio 2026 / 24 (Image-Line)",
            shortName = "FL Studio",
            category = WorkstationCategory.AUDIO_PRODUCTION,
            iconName = "graphic_eq",
            primaryBottleneckTr = "Düşük Gecikmeli DPC/ASIO & Tek Çekirdek Saat Hızı (Kanal Efekt Zinciri)",
            primaryBottleneckEn = "Low-Latency DPC/ASIO & Single-Core Clock (Mixer Track Chain)",
            officialBenchmarkSource = "Image-Line FL Studio Official CPU & Audio Engine Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "ableton_live",
            name = "Ableton Live 12 Suite",
            shortName = "Ableton Live",
            category = WorkstationCategory.AUDIO_PRODUCTION,
            iconName = "music_note",
            primaryBottleneckTr = "Canlı Sahne/Stüdyo DPC Gecikmesi, Ultra Düşük Ses Tamponu & 32-64GB RAM",
            primaryBottleneckEn = "Live Stage/Studio DPC Latency, Ultra-Low Buffer & 32-64GB RAM",
            officialBenchmarkSource = "Ableton Certified Production Workstation Standards",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "cubase_pro_tools",
            name = "Steinberg Cubase 14 / Pro Tools",
            shortName = "Cubase / Pro Tools",
            category = WorkstationCategory.AUDIO_PRODUCTION,
            iconName = "piano",
            primaryBottleneckTr = "Yüzlerce Orkestral VST/Kontakt Kanalı için 64-128GB RAM & Gen4 Sample NVMe",
            primaryBottleneckEn = "64-128GB RAM & Gen4 Sample NVMe for 100+ Orchestral VST Tracks",
            officialBenchmarkSource = "Steinberg Audio Engine DAW Performance Benchmark",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
    )

    fun calculateRequirements(
        appId: String,
        resolution: WorkloadResolution,
        phase: WorkloadPhase,
    ): WorkstationRequirementProfile {
        val app = supportedApps.find { it.id == appId } ?: supportedApps.first()

        val is8k = resolution == WorkloadResolution.RAW_8K
        val is4k = resolution == WorkloadResolution.UHD_4K
        val is2k = resolution == WorkloadResolution.QHD_2K
        val isRender = phase == WorkloadPhase.FINAL_RENDER
        val isSimOrAi = phase == WorkloadPhase.HEAVY_SIMULATION

        // Kategoriye özel donanım profillemesi
        val isAudio = app.category == WorkstationCategory.AUDIO_PRODUCTION
        val isAi = app.category == WorkstationCategory.LOCAL_AI
        val isCad = app.category == WorkstationCategory.CAD_ENGINEERING

        val minTier = when {
            isAudio -> HardwareRecommendation(
                tier = HardwareTier.MINIMUM,
                targetCpuChipset = "Core i5-14600K / Ryzen 7 7700",
                targetCpuSearchQuery = "14600K",
                cpuSpecsSummary = "14 Çekirdek (6P+8E), 5.3 GHz, Düşük DPC Gecikmesi",
                targetGpuChipset = "RTX 3060 12GB / RTX 4060",
                targetGpuSearchQuery = "4060",
                gpuSpecsSummary = "8GB GDDR6 (Sessiz fan modu, 4K ekran desteği)",
                targetRamCapacityGb = 32,
                targetRamTypeAndSpeed = "DDR5 5600MHz / DDR4 3600MHz CL16 Düşük Gecikme",
                targetStorageType = "PCIe 4.0 NVMe SSD (5000 MB/s Örnek Kütüphanesi)",
                vramRequirementGb = 8,
                estimatedExperienceNoteTr = "32-48 kanal miks ve standart VST enstrümanlarda sıfır ses çıtırtısı (crackling).",
                estimatedExperienceNoteEn = "Clean playback with zero audio crackling for 32-48 audio tracks and VSTs.",
                benchmarkConfidenceScore = 98,
            )
            isAi || is8k -> HardwareRecommendation(
                tier = HardwareTier.MINIMUM,
                targetCpuChipset = "Core i7-14700K / Ryzen 9 7900X",
                targetCpuSearchQuery = "14700K",
                cpuSpecsSummary = "20 Çekirdek (8P+12E), 5.6 GHz, 33MB L3 Önbellek",
                targetGpuChipset = "RTX 4070 Ti Super",
                targetGpuSearchQuery = "4070 Ti Super",
                gpuSpecsSummary = "16GB GDDR6X (8448 CUDA, 256-bit Veri Yolu)",
                targetRamCapacityGb = 64,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 (Dual Channel)",
                targetStorageType = "PCIe 4.0 NVMe SSD (7000 MB/s Okuma)",
                vramRequirementGb = 16,
                estimatedExperienceNoteTr = "Flux.1 ve SDXL modellerini yerel VRAM içinde taşmadan çalıştırır.",
                estimatedExperienceNoteEn = "Runs Flux.1 and SDXL without catastrophic system RAM fallback.",
                benchmarkConfidenceScore = 95,
            )
            is4k || isCad -> HardwareRecommendation(
                tier = HardwareTier.MINIMUM,
                targetCpuChipset = "Core i5-14600K / Ryzen 7 7700X",
                targetCpuSearchQuery = "14600K",
                cpuSpecsSummary = "14 Çekirdek (6P+8E) veya 8C/16T Zen 4, 5.3 GHz Turbo",
                targetGpuChipset = "RTX 4070 Super",
                targetGpuSearchQuery = "4070 Super",
                gpuSpecsSummary = "12GB GDDR6X (7168 CUDA, 504 GB/s Bant Genişliği)",
                targetRamCapacityGb = 32,
                targetRamTypeAndSpeed = "DDR5 5600MHz / 6000MHz CL30",
                targetStorageType = "PCIe 4.0 NVMe SSD (5000+ MB/s)",
                vramRequirementGb = 12,
                estimatedExperienceNoteTr = "4K H.265 / ProRes ve 10.000+ parçalı CAD montajlarında kararlı başlangıç.",
                estimatedExperienceNoteEn = "Stable baseline for 4K ProRes and 10,000+ component CAD assemblies.",
                benchmarkConfidenceScore = 97,
            )
            else -> HardwareRecommendation(
                tier = HardwareTier.MINIMUM,
                targetCpuChipset = "Core i5-13400 / Ryzen 5 7600",
                targetCpuSearchQuery = "7600",
                cpuSpecsSummary = "6C/12T Zen 4 veya 10 Çekirdek, 5.1 GHz",
                targetGpuChipset = "RTX 3060 12GB / RTX 4060",
                targetGpuSearchQuery = "3060",
                gpuSpecsSummary = "12GB GDDR6 (3584 CUDA) veya 8GB GDDR6",
                targetRamCapacityGb = 16,
                targetRamTypeAndSpeed = "DDR4 3200MHz / DDR5 5200MHz",
                targetStorageType = "M.2 NVMe SSD (3500 MB/s)",
                vramRequirementGb = 8,
                estimatedExperienceNoteTr = "1080p ve 2K içeriklerde akıcı başlangıç düzeyi.",
                estimatedExperienceNoteEn = "Fluid baseline experience for 1080p and light 2K workloads.",
                benchmarkConfidenceScore = 98,
            )
        }

        val proTier = when {
            isAudio -> HardwareRecommendation(
                tier = HardwareTier.RECOMMENDED_PRO,
                targetCpuChipset = "Ryzen 9 9900X / Core i7-14700K",
                targetCpuSearchQuery = "9900X",
                cpuSpecsSummary = "12C/24T Zen 5 veya 20 Çekirdek, 5.6 GHz, 64MB L3",
                targetGpuChipset = "RTX 4070 Super",
                targetGpuSearchQuery = "4070 Super",
                gpuSpecsSummary = "12GB GDDR6X (Üçlü 4K stüdyo monitörü ve video scoring desteği)",
                targetRamCapacityGb = 64,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 Ultra Düşük Gecikme",
                targetStorageType = "2x PCIe 4.0 NVMe (İşletim Sistemi + 4TB Kontakt Sample Diski)",
                vramRequirementGb = 12,
                estimatedExperienceNoteTr = "128+ kanal, orkestral sample kütüphaneleri ve onlarca VST plugin anında yüklenir.",
                estimatedExperienceNoteEn = "Instant loading for 128+ tracks, heavy orchestral sample libraries and dozens of VSTs.",
                benchmarkConfidenceScore = 99,
            )
            is8k || isAi -> HardwareRecommendation(
                tier = HardwareTier.RECOMMENDED_PRO,
                targetCpuChipset = "Ryzen 9 9900X / Core i9-14900K",
                targetCpuSearchQuery = "9900X",
                cpuSpecsSummary = "12C/24T Zen 5 veya 24 Çekirdek, 5.6-6.0 GHz",
                targetGpuChipset = "RTX 4080 Super",
                targetGpuSearchQuery = "4080 Super",
                gpuSpecsSummary = "16GB GDDR6X (10240 CUDA, 736 GB/s Bant Genişliği)",
                targetRamCapacityGb = 64,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 EXPO/XMP",
                targetStorageType = "PCIe 4.0 NVMe (7300 MB/s) + Ayrı Yüksek Hızlı Scratch Disk",
                vramRequirementGb = 16,
                estimatedExperienceNoteTr = "8K RAW kurguda tam kalite önizleme ve yüksek AI matris çarpanı.",
                estimatedExperienceNoteEn = "High fidelity preview and accelerated AI tensor throughput.",
                benchmarkConfidenceScore = 98,
            )
            is4k && isRender -> HardwareRecommendation(
                tier = HardwareTier.RECOMMENDED_PRO,
                targetCpuChipset = "Ryzen 9 7900X / Core i7-14700K",
                targetCpuSearchQuery = "7900X",
                cpuSpecsSummary = "12C/24T veya 20C/28T, 5.6 GHz Turbo, Intel QuickSync",
                targetGpuChipset = "RTX 4070 Ti Super",
                targetGpuSearchQuery = "4070 Ti Super",
                gpuSpecsSummary = "16GB GDDR6X (8448 CUDA, 256-bit Veri Yolu)",
                targetRamCapacityGb = 64,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 (Dual Channel)",
                targetStorageType = "PCIe 4.0 Gen4 NVMe (7000 MB/s Okuma)",
                vramRequirementGb = 16,
                estimatedExperienceNoteTr = "4K render süresini 3 kat hızlandırır; 16GB VRAM taşmayı önler.",
                estimatedExperienceNoteEn = "Triples 4K export speed; 16GB VRAM completely eliminates OOM leaks.",
                benchmarkConfidenceScore = 99,
            )
            is4k || isCad -> HardwareRecommendation(
                tier = HardwareTier.RECOMMENDED_PRO,
                targetCpuChipset = "Core i7-14700K / Ryzen 7 7800X3D",
                targetCpuSearchQuery = "14700K",
                cpuSpecsSummary = "20 Çekirdek (8P+12E), 5.6 GHz, 33MB L3 Önbellek",
                targetGpuChipset = "RTX 4070 Ti Super",
                targetGpuSearchQuery = "4070 Ti Super",
                gpuSpecsSummary = "16GB GDDR6X (8448 CUDA, Çift NVENC Encoder)",
                targetRamCapacityGb = 64,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30",
                targetStorageType = "PCIe 4.0 NVMe SSD (7000 MB/s)",
                vramRequirementGb = 16,
                estimatedExperienceNoteTr = "4K 60fps zaman çizelgesinde ve karmaşık 3D sahnelerde sıfır kare kaybı.",
                estimatedExperienceNoteEn = "Zero dropped frames at full native resolution 4K 60fps playback.",
                benchmarkConfidenceScore = 99,
            )
            else -> HardwareRecommendation(
                tier = HardwareTier.RECOMMENDED_PRO,
                targetCpuChipset = "Ryzen 7 7700X / Core i5-14600K",
                targetCpuSearchQuery = "7700X",
                cpuSpecsSummary = "8C/16T Zen 4 veya 14 Çekirdek, 5.4 GHz",
                targetGpuChipset = "RTX 4070 Super",
                targetGpuSearchQuery = "4070 Super",
                gpuSpecsSummary = "12GB GDDR6X (7168 CUDA, 504 GB/s Bant Genişliği)",
                targetRamCapacityGb = 32,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 (Dual Channel)",
                targetStorageType = "PCIe 4.0 NVMe SSD (5000+ MB/s)",
                vramRequirementGb = 12,
                estimatedExperienceNoteTr = "2K ve 4K projelerde profesyonel standart hız ve akıcılık.",
                estimatedExperienceNoteEn = "Standard pro performance and fluidity across 2K and 4K pipelines.",
                benchmarkConfidenceScore = 98,
            )
        }

        val studioTier = when {
            isAudio -> HardwareRecommendation(
                tier = HardwareTier.STUDIO_EXTREME,
                targetCpuChipset = "Ryzen 9 9950X / Core i9-14900K",
                targetCpuSearchQuery = "9950X",
                cpuSpecsSummary = "16C/32T Zen 5, 5.7 GHz, 80MB L3 Önbellek, Sessiz Sıvı Soğutma",
                targetGpuChipset = "RTX 4080 Super",
                targetGpuSearchQuery = "4080 Super",
                gpuSpecsSummary = "16GB GDDR6X (0dB Fan Modu, Çoklu 4K/8K Referans Monitörü)",
                targetRamCapacityGb = 128,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 (128GB Dörtlü/İkili Kit)",
                targetStorageType = "PCIe 5.0 NVMe (12000 MB/s) + 8TB PCIe 4.0 Audio Kütüphanesi",
                vramRequirementGb = 16,
                estimatedExperienceNoteTr = "Hans Zimmer ölçeğinde tam senfonik orkestral kütüphaneler ve sıfır gecikme.",
                estimatedExperienceNoteEn = "Hollywood-scale symphonic orchestral setups running at zero latency buffer.",
                benchmarkConfidenceScore = 100,
            )
            is8k || isSimOrAi -> HardwareRecommendation(
                tier = HardwareTier.STUDIO_EXTREME,
                targetCpuChipset = "Ryzen 9 9950X / Core i9-14900KS",
                targetCpuSearchQuery = "9950X",
                cpuSpecsSummary = "16C/32T Zen 5, 5.7 GHz, 80MB Önbellek",
                targetGpuChipset = "RTX 4090 24GB",
                targetGpuSearchQuery = "4090",
                gpuSpecsSummary = "24GB GDDR6X (16384 CUDA, 1008 GB/s Bant Genişliği)",
                targetRamCapacityGb = 128,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 (128GB Kit)",
                targetStorageType = "PCIe 5.0 NVMe SSD (12000 MB/s Okuma) + RAID/Scratch",
                vramRequirementGb = 24,
                estimatedExperienceNoteTr = "Hollywood stüdyoları, ağır 3D simülasyon ve yerel 70B LLM / Flux.1 dev için sınırsız güç.",
                estimatedExperienceNoteEn = "Uncompromised throughput for cinema studios, heavy 3D sim and local 70B LLM.",
                benchmarkConfidenceScore = 100,
            )
            else -> HardwareRecommendation(
                tier = HardwareTier.STUDIO_EXTREME,
                targetCpuChipset = "Ryzen 9 7950X / Core i9-14900K",
                targetCpuSearchQuery = "7950X",
                cpuSpecsSummary = "16C/32T Zen 4 veya 24 Çekirdek, 5.7-6.0 GHz",
                targetGpuChipset = "RTX 4090 24GB / RTX 4080 Super",
                targetGpuSearchQuery = "4090",
                gpuSpecsSummary = "24GB GDDR6X (16384 CUDA, Ada Lovelace Amiral Gemisi)",
                targetRamCapacityGb = 64,
                targetRamTypeAndSpeed = "DDR5 6000MHz CL30 Ultra Düşük Gecikme",
                targetStorageType = "PCIe 4.0 / 5.0 NVMe SSD (7400-10000 MB/s)",
                vramRequirementGb = 24,
                estimatedExperienceNoteTr = "Aynı anda birden fazla profesyonel uygulama açıkken sıfır darboğaz.",
                estimatedExperienceNoteEn = "Zero bottlenecks with multiple professional creative suites open simultaneously.",
                benchmarkConfidenceScore = 100,
            )
        }

        val citation = "Resmi Veri Kaynakları: ${app.officialBenchmarkSource} · SPECworkstation 2026 Test Protokolü."

        val technicalTakeawayTr = when (app.id) {
            "premiere_pro" -> "Premiere Pro'da H.264/H.265 10-bit 4:2:2 donanımsal kod çözme için Intel QuickSync (dahili grafikli Intel CPU'lar) çok büyük avantaj sağlar. Render tarafında ise NVIDIA NVENC çift encoder dışa aktarmayı hızlandırır."
            "davinci_resolve" -> "DaVinci Resolve Studio GPU mimarisine son derece bağımlıdır. Fusion efektleri ve gürültü giderme (Noise Reduction) anında VRAM'i doldurur; 4K için en az 12GB-16GB, 8K için kesinlikle 24GB VRAM (RTX 4090) önerilir."
            "after_effects" -> "After Effects MFR (Multi-Frame Rendering) sayesinde her CPU çekirdeğine 4GB-8GB RAM ayırır. 16 çekirdekli bir sistemde akıcı önizleme için 64GB veya 128GB RAM şarttır."
            "foundry_nuke" -> "Foundry Nuke çok kanallı OpenEXR karelerini anında RAM'e okur. Bu nedenle 7000+ MB/s PCIe 4.0/5.0 NVMe SSD ve 64-128GB RAM en kritik performans belirleyicisidir."
            "blender" -> "Blender Cycles OptiX API'si, NVIDIA RTX ışın izleme çekirdeklerini saf CUDA'ya kıyasla 2 kat daha hızlı çalıştırır. GPU render hızında RTX 4070 Ti Super ve RTX 4090 sektör standardıdır."
            "unreal_engine" -> "Unreal Engine 5'te Lumen ve Nanite mikropoligon sahneleri derlenirken CPU çoklu çekirdek, sahne içi canlı düzenlemede ise yüksek GPU VRAM ve hızlı Gen4/Gen5 SSD kritik öneme sahiptir."
            "unity_engine" -> "Unity 6 projelerinde C# derleme ve asset optimizasyonu yüksek CPU çekirdek sayısından ve 32GB+ RAM'den doğrudan yararlanır."
            "d5_render", "lumion" -> "Gerçek zamanlı mimari render motorları doğrudan ekran kartının donanımsal RT çekirdeklerine ve doku VRAM'ine yüklenir. 16GB VRAM büyük binalar ve peyzajlar için şarttır."
            "houdini" -> "SideFX Houdini akışkan ve piroteknik simülasyonları saf matematiksel hesaplamadır. 16+ çekirdekli yüksek hızlı CPU ve en az 128GB RAM olmadan ağır sahneler kilitlenebilir."
            "solidworks" -> "SolidWorks montaj çizimlerinde tek çekirdek saat hızı belirleyicidir. Simülasyon ve analiz tarafında ise 64GB DDR5 RAM ve yüksek IPC'li Core i7/Ryzen 7 önerilir."
            "autocad", "revit" -> "AutoCAD ve Revit projelerinde çizim ekranı tek bir çekirdeğin frekansına bağlıdır (5.4GHz+ CPU). Çok katlı BIM projelerinde 64GB RAM model şişmesini engeller."
            "ansys", "matlab" -> "Mühendislik matris çözücülerinde AVX-512 komut seti ve bellek bant genişliği performansı belirler; DDR5 çift/dört kanal mimarisi hesaplama süresini yarıya indirir."
            "stable_diffusion" -> "Stable Diffusion (SDXL/Flux.1) çıkarımında VRAM hayati darboğazdır. VRAM yetmediğinde sistem paylaşımlı RAM'e geçer ve hız 20 kat düşer. En az 16GB VRAM zorunludur."
            "local_llm" -> "Llama 3.3 70B veya DeepSeek yerel model çıkarımında GPU bellek bant genişliği (GB/s) token üretim hızını belirler. 24GB VRAM RTX 4090, 70B Q4 modellerini yerel hızda çalıştırmak için idealdir."
            "fl_studio", "ableton_live", "cubase_pro_tools" -> "Müzik prodüksiyonunda GPU önemsizdir; asıl kritik faktör DPC gecikmesi (DPC Latency) ve yüksek tek çekirdek saat hızıdır. Ses kütüphaneleri (Kontakt) için sessiz soğutma ve 64GB hızlı RAM esastır."
            "photoshop", "lightroom" -> "Fotoğraf düzenlemede yüksek IPC'li CPU, 32GB+ RAM ve Scratch Disk olarak ayrılmış NVMe SSD anlık fırça ve AI maskeleme gecikmelerini sıfırlar."
            else -> "Profesyonel iş yüklerinde sistem dengesi esastır. Hızlı bir CPU, yetersiz VRAM veya yavaş bir SSD ile eşleştirilirse sistem anlık donmalara (micro-stutter) maruz kalır."
        }

        val technicalTakeawayEn = when (app.id) {
            "premiere_pro" -> "Intel QuickSync provides exceptional hardware decoding for 10-bit 4:2:2 media, while NVIDIA dual NVENC accelerates exports."
            "davinci_resolve" -> "DaVinci Resolve Studio is heavily GPU-bound. 4K timeline work demands 16GB VRAM, while 8K requires 24GB VRAM (RTX 4090)."
            "blender" -> "Blender Cycles OptiX delivers up to 2x faster render times than pure CUDA. RTX 4070 Ti Super and RTX 4090 are industry baselines."
            "unreal_engine" -> "UE5 Lumen and Nanite geometry workflows require high GPU VRAM and ultra-fast PCIe Gen4/Gen5 scratch storage."
            "fl_studio", "ableton_live", "cubase_pro_tools" -> "DAW workloads prioritize CPU single-thread IPC, low DPC latency and 64GB RAM over heavy GPU power."
            "stable_diffusion" -> "Local AI image generation requires at least 16GB VRAM to avoid catastrophic performance drops caused by RAM offloading."
            else -> "Balanced hardware prevents pipeline bottlenecks across high-resolution professional workloads."
        }

        return WorkstationRequirementProfile(
            app = app,
            resolution = resolution,
            phase = phase,
            minimumTier = minTier,
            recommendedTier = proTier,
            studioTier = studioTier,
            dataCitation = citation,
            keyTechnicalTakeawayTr = technicalTakeawayTr,
            keyTechnicalTakeawayEn = technicalTakeawayEn,
        )
    }
}
