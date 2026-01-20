# SiteUp Cloud 微服务快速启动脚本
# 用于开发环境快速启动所有服务

Write-Host "🚀 SiteUp Cloud 微服务启动脚本" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green

# 检查Java环境
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java环境检查通过: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java环境未找到，请安装JDK 21+" -ForegroundColor Red
    exit 1
}

# 检查Maven环境
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✅ Maven环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven环境未找到，请安装Maven 3.6+" -ForegroundColor Red
    exit 1
}

# 编译项目
Write-Host "`n📦 编译项目..." -ForegroundColor Yellow
mvn clean compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 项目编译失败" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 项目编译成功" -ForegroundColor Green

# 启动服务顺序
$services = @(
    @{Name="网关服务"; Path="siteup-gateway"; Port="8010"},
    @{Name="认证服务"; Path="siteup-auth"; Port="8020"},
    @{Name="业务服务"; Path="siteup-biz"; Port="8030"},
    @{Name="引擎服务"; Path="siteup-engine"; Port="8040"}
)

Write-Host "`n🔄 按顺序启动微服务..." -ForegroundColor Yellow

foreach ($service in $services) {
    Write-Host "启动 $($service.Name) (端口: $($service.Port))..." -ForegroundColor Cyan

    # 启动服务（后台运行）
    $job = Start-Job -ScriptBlock {
        param($path, $port)
        Set-Location $path
        mvn spring-boot:run
    } -ArgumentList $service.Path, $service.Port

    # 等待服务启动
    Start-Sleep -Seconds 15

    # 检查服务是否启动成功
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $($service.Name) 启动成功 (端口: $($service.Port))" -ForegroundColor Green
        } else {
            Write-Host "⚠️ $($service.Name) 响应异常" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ $($service.Name) 启动失败或未就绪" -ForegroundColor Red
    }
}

Write-Host "`n🎉 所有服务启动完成！" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host "服务访问地址:" -ForegroundColor Cyan
Write-Host "  🌐 网关入口: http://localhost:8010" -ForegroundColor White
Write-Host "  📊 API文档: http://localhost:8030/swagger-ui.html" -ForegroundColor White
Write-Host "  🎛️ Nacos控制台: http://localhost:8848/nacos" -ForegroundColor White
Write-Host "  📈 Sentinel控制台: http://localhost:8080" -ForegroundColor White
Write-Host "  🔍 Zipkin链路追踪: http://localhost:9411" -ForegroundColor White
Write-Host "`n💡 测试建议:" -ForegroundColor Yellow
Write-Host "  1. 导入项目根目录的 siteup_microservices.json 到Postman" -ForegroundColor White
Write-Host "  2. 运行'用户注册'和'用户登录'请求" -ForegroundColor White
Write-Host "  3. 尝试'从模板创建项目'和'发布项目'" -ForegroundColor White
Write-Host "  4. 查看生成的历史记录和统计信息" -ForegroundColor White
Write-Host "`n⚠️ 注意: 按 Ctrl+C 停止所有服务" -ForegroundColor Yellow

# 保持脚本运行，显示服务状态
Write-Host "`n🔍 监控服务状态 (按 Ctrl+C 退出)..." -ForegroundColor Cyan
while ($true) {
    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "$(Get-Date -Format 'HH:mm:ss') - $($service.Name): ✅ 正常" -ForegroundColor Green
            } else {
                Write-Host "$(Get-Date -Format 'HH:mm:ss') - $($service.Name): ⚠️ 异常" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "$(Get-Date -Format 'HH:mm:ss') - $($service.Name): ❌ 离线" -ForegroundColor Red
        }
    }
    Start-Sleep -Seconds 30
}
