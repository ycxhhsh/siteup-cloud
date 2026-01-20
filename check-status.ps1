# SiteUp Cloud 环境状态检查脚本

Write-Host "🔍 SiteUp Cloud 环境状态检查" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

# 检查MySQL服务
Write-Host "`n1. 数据库状态检查:" -ForegroundColor Yellow
try {
    $mysqlResult = mysql -u root -p123456 -e "SELECT VERSION();" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ MySQL服务: 运行中" -ForegroundColor Green

        # 检查数据库
        $dbCheck = mysql -u root -p123456 -e "SHOW DATABASES LIKE 'siteup_%';" 2>$null
        if ($dbCheck -match "siteup_") {
            Write-Host "   ✅ 数据库: 已创建" -ForegroundColor Green
            Write-Host "      - siteup_auth (认证服务)" -ForegroundColor White
            Write-Host "      - siteup_biz (业务服务)" -ForegroundColor White
            Write-Host "      - siteup_engine (引擎服务)" -ForegroundColor White
        } else {
            Write-Host "   ❌ 数据库: 未初始化" -ForegroundColor Red
            Write-Host "      运行: mysql -u root -p < database-init.sql" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ❌ MySQL服务: 未运行" -ForegroundColor Red
    }
} catch {
    Write-Host "   ❌ MySQL连接失败" -ForegroundColor Red
}

# 检查Nacos服务
Write-Host "`n2. Nacos服务检查:" -ForegroundColor Yellow
try {
    $nacosResponse = Invoke-WebRequest -Uri "http://localhost:8848/nacos" -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($nacosResponse.StatusCode -eq 200) {
        Write-Host "   ✅ Nacos服务: 运行中 (端口: 8848)" -ForegroundColor Green
        Write-Host "      控制台: http://localhost:8848/nacos" -ForegroundColor White
    } else {
        Write-Host "   ❌ Nacos服务: 未运行" -ForegroundColor Red
    }
} catch {
    Write-Host "   ❌ Nacos服务: 未运行或连接失败" -ForegroundColor Red
    Write-Host "      启动命令: sh startup.sh -m standalone" -ForegroundColor Yellow
}

# 检查微服务状态
Write-Host "`n3. 微服务状态检查:" -ForegroundColor Yellow
$services = @(
    @{Name="网关服务"; Port="8010"},
    @{Name="认证服务"; Port="8020"},
    @{Name="业务服务"; Port="8030"},
    @{Name="引擎服务"; Port="8040"}
)

$allServicesRunning = $true
foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -TimeoutSec 3 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "   ✅ $($service.Name): 运行中 (端口: $($service.Port))" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️ $($service.Name): 响应异常 (端口: $($service.Port))" -ForegroundColor Yellow
            $allServicesRunning = $false
        }
    } catch {
        Write-Host "   ❌ $($service.Name): 未运行 (端口: $($service.Port))" -ForegroundColor Red
        $allServicesRunning = $false
    }
}

# 检查Nacos中的服务注册
Write-Host "`n4. 服务注册检查:" -ForegroundColor Yellow
try {
    # 这里可以调用Nacos API检查服务注册状态
    Write-Host "   ℹ️ 请访问 http://localhost:8848/nacos 检查服务注册情况" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ 无法检查服务注册状态" -ForegroundColor Red
}

# 总结和建议
Write-Host "`n📋 环境状态总结:" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green

if ($allServicesRunning) {
    Write-Host "🎉 恭喜！所有服务都正常运行" -ForegroundColor Green
    Write-Host "`n🚀 现在可以开始测试:" -ForegroundColor Yellow
    Write-Host "   1. 导入 siteup_microservices.json 到Postman" -ForegroundColor White
    Write-Host "   2. 运行 '用户注册' 和 '用户登录' 请求" -ForegroundColor White
    Write-Host "   3. 尝试 '从模板创建项目' 和 '发布项目'" -ForegroundColor White
    Write-Host "   4. 查看生成历史: GET /api/generate/history" -ForegroundColor White
} else {
    Write-Host "⚠️ 部分服务未正常运行" -ForegroundColor Yellow
    Write-Host "`n🔧 修复建议:" -ForegroundColor Cyan
    Write-Host "   1. 确保数据库已初始化: mysql -u root -p < database-init.sql" -ForegroundColor White
    Write-Host "   2. 确保Nacos已启动: cd nacos/bin && sh startup.sh -m standalone" -ForegroundColor White
    Write-Host "   3. 按顺序启动服务: ./start-services.ps1" -ForegroundColor White
}

Write-Host "`n📞 技术支持:" -ForegroundColor Cyan
Write-Host "   如遇问题，请检查服务日志或联系开发者" -ForegroundColor White
