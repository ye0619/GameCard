"""
GameCard — AI Background Removal Service

独立 Python 微服务，使用 rembg 实现角色图片自动去背景。
返回透明 PNG。

启动:
    cd python-image-service
    pip install -r requirements.txt
    python app.py          # 默认 http://localhost:8001
"""

import io
import logging
from pathlib import Path

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import Response

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("bg-remover")

app = FastAPI(
    title="GameCard Background Remover",
    description="使用 rembg 自动去除图片背景，返回透明 PNG",
    version="1.0.0",
)

# CORS — 允许 Spring Boot 后端跨域调用
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["POST"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------
# 延迟加载 rembg session（只在首次请求时加载，减少内存占用）
# ---------------------------------------------------------------
_session = None


def get_session():
    global _session
    if _session is None:
        from rembg import new_session

        logger.info("Loading rembg model (u2net)…")
        _session = new_session("u2net")
        logger.info("rembg model loaded.")
    return _session


# ---------------------------------------------------------------
# POST /api/remove-background
# ---------------------------------------------------------------


@app.post("/api/remove-background")
async def remove_background(image: UploadFile = File(...)):
    """
    接收上传图片，去除背景后返回透明 PNG。

    - 请求: multipart/form-data, field name = 'image'
    - 响应: image/png (透明 PNG 字节流)
    """
    # 1. 校验文件类型
    if not image.content_type or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="仅支持图片文件")

    # 2. 读取原始字节
    raw_bytes = await image.read()
    if len(raw_bytes) == 0:
        raise HTTPException(status_code=400, detail="空文件")

    logger.info(f"Processing: {image.filename} ({len(raw_bytes)} bytes)")

    try:
        session = get_session()
        from rembg import remove

        # 3. 执行去背景
        result_bytes = remove(raw_bytes, session=session)

        # 4. 校验返回是否为有效 PNG
        if not result_bytes or len(result_bytes) < 50:
            raise HTTPException(status_code=500, detail="去背景处理结果为空")

        logger.info(f"Done: {image.filename} -> {len(result_bytes)} bytes PNG")

        return Response(
            content=result_bytes,
            media_type="image/png",
            headers={
                "X-Original-Filename": image.filename or "image.png",
            },
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.exception("Background removal failed")
        raise HTTPException(status_code=500, detail=f"去背景处理失败: {str(e)}")


# ---------------------------------------------------------------
# GET /api/health
# ---------------------------------------------------------------


@app.get("/api/health")
async def health():
    return {"status": "ok", "service": "bg-remover"}


# ---------------------------------------------------------------
# 主入口
# ---------------------------------------------------------------

if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app:app", host="0.0.0.0", port=8001, log_level="info")
